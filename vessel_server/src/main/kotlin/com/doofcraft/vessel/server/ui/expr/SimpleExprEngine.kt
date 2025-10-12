package com.doofcraft.vessel.server.ui.expr

import kotlinx.datetime.Clock
import kotlin.collections.get
import kotlin.math.floor

class SimpleExprEngine : ExprEngine {
    override fun eval(expr: String, scope: Scope): Any? {
        val p = Parser(Tokenize(expr))
        val node = p.parseExpression()
        val result = Eval.eval(node, scope)
        return result
    }

    override fun renderTemplate(text: String, scope: Scope): String {
        return Template.render(text, this, scope)
    }

    private enum class T {
        EOF, ID, NUM, STR, TRUE, FALSE, NULL, LPAR, RPAR, QMARK, ELVIS, COLON, COMMA, PLUS, MINUS, STAR, SLASH, PERCENT, BANG, EQ, EQEQ, NEQ, LT, LTE, GT, GTE, ANDAND, OROR, DOT
    }

    private data class Tok(val t: T, val s: String, val pos: Int)

    private class Tokenize(private val src: String) {
        private var i = 0
        private val n = src.length

        private fun isIdStart(c: Char) = c == '_' || c.isLetter() || c == '@'
        private fun isIdPart(c: Char) = isIdStart(c) || c.isDigit()

        fun next(): Tok {
            skipWs()
            if (i >= n) return Tok(T.EOF, "", i)

            val c = src[i]
            // numbers
            if (c.isDigit()) {
                val start = i; i++
                while (i < n && (src[i].isDigit())) i++
                if (i < n && src[i] == '.') {
                    i++; while (i < n && src[i].isDigit()) i++
                }
                return Tok(T.NUM, src.substring(start, i), start)
            }
            // id / keywords
            if (isIdStart(c)) {
                val start = i; i++
                while (i < n && isIdPart(src[i])) i++
                val s = src.substring(start, i)
                return when (s) {
                    "true" -> Tok(T.TRUE, s, start)
                    "false" -> Tok(T.FALSE, s, start)
                    "null" -> Tok(T.NULL, s, start)
                    else -> Tok(T.ID, s, start)
                }
            }
            // strings
            if (c == '"' || c == '\'') {
                val quote = c;
                val start = i; i++
                val sb = StringBuilder()
                while (i < n && src[i] != quote) {
                    val ch = src[i++]
                    if (ch == '\\' && i < n) {
                        val e = src[i++]
                        sb.append(
                            when (e) {
                                '\\' -> '\\'; '"' -> '"'; '\'' -> '\''; 'n' -> '\n'; 't' -> '\t'; 'r' -> '\r'
                                else -> e
                            }
                        )
                    } else sb.append(ch)
                }
                if (i < n && src[i] == quote) i++
                return Tok(T.STR, sb.toString(), start)
            }

            fun two(ch: Char, t: T): Tok {
                i += 2; return Tok(t, "$c$ch", i - 2)
            }

            fun one(t: T): Tok {
                i++; return Tok(t, "$c", i - 1)
            }

            return when (c) {
                '(' -> one(T.LPAR)
                ')' -> one(T.RPAR)
                '?' -> if (peek('?')) two('?', T.ELVIS) else one(T.QMARK)
                ':' -> one(T.COLON)
                ',' -> one(T.COMMA)
                '.' -> one(T.DOT)
                '+' -> one(T.PLUS)
                '-' -> one(T.MINUS)
                '*' -> one(T.STAR)
                '/' -> one(T.SLASH)
                '%' -> one(T.PERCENT)
                '!' -> if (peek('=')) two('=', T.NEQ) else one(T.BANG)
                '=' -> if (peek('=')) two('=', T.EQEQ) else one(T.EQ)
                '<' -> if (peek('=')) two('=', T.LTE) else one(T.LT)
                '>' -> if (peek('=')) two('=', T.GTE) else one(T.GT)
                '&' -> if (peek('&')) two('&', T.ANDAND) else err("Unexpected '&'")
                '|' -> if (peek('|')) two('|', T.OROR) else err("Unexpected '|'")
                else -> err("Unexpected '$c'")
            }
        }

        private fun skipWs() {
            while (i < n && src[i].isWhitespace()) i++
        }

        private fun peek(ch: Char): Boolean = i + 1 < n && src[i + 1] == ch
        private fun err(msg: String): Nothing = throw IllegalArgumentException("$msg at $i in `$src`")
    }

    private sealed interface Node {
        data class Lit(val v: Any?) : Node
        data class Var(val parts: List<String>) : Node
        data class GetProp(val base: Node, val name: String) : Node
        data class Call(val fn: String, val args: List<Node>) : Node
        data class Unary(val op: T, val a: Node) : Node
        data class Bin(val op: T, val a: Node, val b: Node) : Node
        data class Ternary(val c: Node, val t: Node, val f: Node) : Node
    }

    private class Parser(private val lx: Tokenize) {
        private var la: Tok = lx.next()

        fun parseExpression(): Node = parseTernary()

        private fun parseTernary(): Node {
            var e = parseOr()
            if (la.t == T.QMARK) {
                eat(T.QMARK)
                val t = parseExpression()
                eat(T.COLON)
                val f = parseExpression()
                e = Node.Ternary(e, t, f)
            }
            return e
        }

        private fun parseOr(): Node {
            var e = parseAnd()
            while (la.t == T.OROR) {
                val op = la.t; eat(op); e = Node.Bin(op, e, parseAnd())
            }
            return e
        }

        private fun parseAnd(): Node {
            var e = parseEq()
            while (la.t == T.ANDAND) {
                val op = la.t; eat(op); e = Node.Bin(op, e, parseEq())
            }
            return e
        }

        private fun parseEq(): Node {
            var e = parseRel()
            while (la.t == T.EQEQ || la.t == T.NEQ) {
                val op = la.t; eat(op); e = Node.Bin(op, e, parseRel())
            }
            return e
        }

        private fun parseRel(): Node {
            var e = parseAdd()
            while (la.t in setOf(T.LT, T.LTE, T.GT, T.GTE)) {
                val op = la.t; eat(op); e = Node.Bin(op, e, parseAdd())
            }
            return e
        }

        private fun parseAdd(): Node {
            var e = parseMul()
            while (la.t == T.PLUS || la.t == T.MINUS) {
                val op = la.t
                eat(op)
                e = Node.Bin(op, e, parseMul())
            }
            return e
        }

        private fun parseMul(): Node {
            var e = parseElvis()
            while (la.t == T.STAR || la.t == T.SLASH || la.t == T.PERCENT) {
                val op = la.t
                eat(op)
                e = Node.Bin(op, e, parseUnary())
            }
            return e
        }

        private fun parseElvis(): Node {
            var e = parseUnary()
            while (la.t == T.ELVIS) {
                val op = la.t
                eat(op)
                e = Node.Bin(op, e, parseUnary())
            }
            return e
        }

        private fun parseUnary(): Node {
            return when (la.t) {
                T.BANG, T.MINUS -> {
                    val op = la.t; eat(op); Node.Unary(op, parseUnary())
                }

                else -> parsePostfix()
            }
        }

        private fun parsePostfix(): Node {
            var e = parsePrimary()
            loop@ while (true) {
                when (la.t) {
                    T.DOT -> {
                        eat(T.DOT);
                        val id = expect(T.ID); e = Node.GetProp(e, id.s)
                    }

                    T.LPAR -> {
                        // simple function call i.e. fn(...)
                        if (e is Node.Var && e.parts.size == 1) {
                            val fn = e.parts.first()
                            eat(T.LPAR)
                            val args = mutableListOf<Node>()
                            if (la.t != T.RPAR) {
                                do {
                                    args += parseExpression()
                                } while (accept(T.COMMA))
                            }
                            eat(T.RPAR)
                            e = Node.Call(fn, args)
                        } else {
                            throw IllegalArgumentException("Only simple function calls supported")
                        }
                    }

                    else -> break@loop
                }
            }
            return e
        }

        private fun parsePrimary(): Node {
            return when (la.t) {
                T.NUM -> {
                    val s = la.s; eat(T.NUM); Node.Lit(s.toDouble())
                }

                T.STR -> {
                    val s = la.s; eat(T.STR); Node.Lit(s)
                }

                T.TRUE -> {
                    eat(T.TRUE); Node.Lit(true)
                }

                T.FALSE -> {
                    eat(T.FALSE); Node.Lit(false)
                }

                T.NULL -> {
                    eat(T.NULL); Node.Lit(null)
                }

                T.ID -> {
                    val id = la.s; eat(T.ID)
                    Node.Var(listOf(id))
                }

                T.LPAR -> {
                    eat(T.LPAR);
                    val e = parseExpression(); eat(T.RPAR); e
                }

                else -> throw IllegalArgumentException("Unexpected token ${la.t} at ${la.pos}")
            }
        }

        private fun eat(t: T) {
            if (la.t != t) throw IllegalArgumentException("Expected $t got ${la.t}")
            la = lx.next()
        }

        private fun accept(t: T): Boolean {
            if (la.t == t) {
                la = lx.next()
                return true
            }
            return false
        }

        private fun expect(t: T): Tok {
            if (la.t != t) throw IllegalArgumentException("Expected $t got ${la.t}")
            val r = la; la = lx.next(); return r
        }
    }

    private object Eval {
        fun eval(n: Node, sc: Scope): Any? = when (n) {
            is Node.Lit -> n.v
            is Node.Var -> resolveVar(n.parts, sc)
            is Node.GetProp -> {
                val base = eval(n.base, sc)
                when (base) {
                    is Map<*, *> -> base[n.name]
                    else -> null
                }
            }

            is Node.Call -> call(n.fn, n.args.map { eval(it, sc) }, sc)
            is Node.Unary -> when (n.op) {
                T.BANG -> !truthy(eval(n.a, sc))
                T.MINUS -> -num(eval(n.a, sc))
                else -> error("bad unary")
            }

            is Node.Bin -> {
                val a = eval(n.a, sc)
                when (n.op) {
                    T.PLUS -> plus(a, eval(n.b, sc))
                    T.MINUS -> num(a) - num(eval(n.b, sc))
                    T.STAR -> num(a) * num(eval(n.b, sc))
                    T.SLASH -> num(a) / num(eval(n.b, sc))
                    T.PERCENT -> num(a) % num(eval(n.b, sc))
                    T.EQEQ -> eq(a, eval(n.b, sc))
                    T.NEQ -> !eq(a, eval(n.b, sc))
                    T.LT -> num(a) < num(eval(n.b, sc))
                    T.LTE -> num(a) <= num(eval(n.b, sc))
                    T.GT -> num(a) > num(eval(n.b, sc))
                    T.GTE -> num(a) >= num(eval(n.b, sc))
                    T.ANDAND -> if (truthy(a)) eval(n.b, sc) else a
                    T.OROR -> if (truthy(a)) a else eval(n.b, sc)
                    T.ELVIS -> a ?: eval(n.b, sc)
                    else -> error("bad bin")
                }
            }

            is Node.Ternary -> if (truthy(eval(n.c, sc))) eval(n.t, sc) else eval(n.f, sc)
        }

        private fun resolveVar(path: List<String>, sc: Scope): Any? {
            val head = path.first()
            val (root, offset) = when (head) {
                "@value" -> sc.value to 1
                "@state" -> sc.state to 1
                "@params" -> sc.params to 1
                "@player" -> sc.player to 1
                "@menu" -> sc.menu to 1
                "@data" -> sc.nodeValues to 1
                else -> emptyMap<String, Any?>() to 0
            }
            var cur: Any? = root
            var i = offset
            while (i < path.size) {
                cur = when (cur) {
                    is Map<*, *> -> cur[path[i]]
                    is List<*> -> {
                        val idx = path[i].toIntOrNull() ?: return null
                        cur.getOrNull(idx)
                    }

                    else -> return null
                }
                i++
            }
            return cur
        }

        private fun call(fn: String, args: List<Any?>, sc: Scope): Any? = when (fn) {
            "lookup" -> {
                // lookup(nodeId, key, default)
                val node = args.getOrNull(0) ?: return null
                val keyAny = args.getOrNull(1)
                val default = args.getOrNull(2)
                when (node) {
                    is Map<*, *> -> if (keyAny == null) node else smartGet(node, keyAny) ?: default
                    is List<*> -> {
                        val intKey = keyAny as? Int ?: keyAny?.toString()?.toIntOrNull()
                        if (intKey != null) node[intKey] ?: default
                        else default
                    }
                    else -> default
                }
            }

            "len" -> {
                val v = args.getOrNull(0)
                when (v) {
                    is String -> v.length
                    is Collection<*> -> v.size
                    is Map<*, *> -> v.size
                    else -> 0
                }
            }

            "has" -> {
                val nodeId = args.getOrNull(0)?.toString() ?: return false
                sc.nodeValues.containsKey(nodeId)
            }

            "now" -> Clock.System.now().epochSeconds

            "max" -> {
                val a = num(args.getOrNull(0)).toLong();
                val b = num(args.getOrNull(1)).toLong()
                kotlin.math.max(a, b)
            }

            "fmax" -> {
                val a = num(args.getOrNull(0))
                val b = num(args.getOrNull(1))
                kotlin.math.max(a, b)
            }

            "pluck" -> {
                val list = args.getOrNull(0) as? List<*> ?: return emptyList<Any?>()
                val path = args.getOrNull(1)?.toString() ?: return emptyList<Any?>()
                list.map { row ->
                    val m = row as? Map<*, *> ?: return@map null
                    getByPath(m, path)
                }
            }

            else -> null
        }

        private fun truthy(v: Any?): Boolean = when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            is Collection<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }

        private fun num(v: Any?): Double = when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: 0.0
            is Boolean -> if (v) 1.0 else 0.0
            null -> 0.0
            else -> 0.0
        }

        private fun plus(a: Any?, b: Any?): Any {
            // string concat if any is string
            if (a is String || b is String) return toStr(a) + toStr(b)
            return num(a) + num(b)
        }

        private fun eq(a: Any?, b: Any?): Boolean {
            return when {
                a is Number || b is Number -> num(a) == num(b)
                else -> a == b
            }
        }

        private fun toStr(v: Any?): String = when (v) {
            null -> ""
            is String -> v
            is Number -> {
                val d = v.toDouble()
                if (floor(d) == d) d.toLong().toString() else d.toString()
            }

            is Boolean -> if (v) "true" else "false"
            else -> v.toString()
        }

        private fun getByPath(root: Map<*, *>, path: String): Any? {
            var cur: Any? = root
            for (seg in path.split('.')) {
                cur = when (cur) {
                    is Map<*, *> -> cur[seg]
                    is List<*> -> seg.toIntOrNull()?.let { idx -> cur.getOrNull(idx) }
                    else -> return null
                }
            }
            return cur
        }

        // Map[key] but with some attempted conversions if Map doesn't contain Key.
        private fun smartGet(map: Map<*, *>, key: Any?): Any? {
            if (map.containsKey(key)) return map[key]

            if (key is Number) {
                val kLong = key.toLong()
                map[kLong]?.let { return it }
                val kInt = kLong.toInt()
                map[kInt]?.let { return it }
                val kDouble = key.toDouble()
                map[kDouble]?.let { return it }
                map.entries.firstOrNull {
                    it.key is Number && (it.key as Number).toLong() == kLong
                }?.let { return it.value }
            }

            if (key is String) {
                key.toLongOrNull()?.let { l ->
                    map[l]?.let { return it }
                    map[l.toInt()]?.let { return it }
                }
            }

            val ks = key?.toString()
            if (ks != null) {
                map.entries.firstOrNull {
                    it.key?.toString() == ks
                }?.let { return it.value }
            }

            return null
        }
    }

    private object Template {
        // Ternary regex is complex to avoid capturing part of an Identifier ('abc:def') as the expression separator.
        private val blockRegex =
            Regex("""\{\?\s*(.*?)\s*\?\s*((?:'[^']*'|[^':{}])*?)\s*:\s*((?:'[^']*'|[^{}])*?)\s*}""")
        private val holeRegex = Regex("""\{([^{}]+)}""")

        fun render(input: String, engine: ExprEngine, scope: Scope): String {
            // First resolve conditional blocks (greedy left-to-right, supports nesting by re-run)
            var s = input
            var changed: Boolean
            do {
                changed = false
                s = blockRegex.replace(s) { m ->
                    changed = true
                    val cond = m.groupValues[1].trim()
                    val t = m.groupValues[2].trim('\'').trim()
                    val f = m.groupValues[3].trim('\'').trim()
                    val ok = truthy(engine.eval(cond, scope))
                    // recursively render the chosen branch
                    render(if (ok) t else f, engine, scope)
                }
            } while (changed)
            // Then simple { expr } holes
            s = holeRegex.replace(s) { m ->
                val expr = m.groupValues[1].trim()
                val v = engine.eval(expr, scope)
                when (v) {
                    null -> ""
                    is String -> v
                    is Number, is Boolean -> v.toString()
                    else -> v.toString()
                }
            }
            return s
        }

        private fun truthy(v: Any?): Boolean = when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            is Collection<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }
    }
}