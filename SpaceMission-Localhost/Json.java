import java.util.*;

// Tiny JSON helper (no external libraries needed).
// stringify(): turns Java Maps / Lists / Strings / Numbers into JSON text for the browser.
// parseObject(): reads a flat JSON object sent by the browser, e.g. {"id":"M1","fuel":7000}.
public class Json {

    // ---------- Java -> JSON ----------
    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof Number || o instanceof Boolean) {
            sb.append(o);
        } else if (o instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                quote(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (o instanceof Collection<?> list) {
            sb.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(',');
                first = false;
                write(sb, item);
            }
            sb.append(']');
        } else {
            quote(sb, o.toString());
        }
    }

    private static void quote(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ---------- JSON -> Java ----------
    public static Map<String, Object> parseObject(String text) {
        return new Parser(text).object();
    }

    private static class Parser {
        private final String s;
        private int i = 0;

        Parser(String s) {
            this.s = s;
        }

        private void skipSpaces() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private void expect(char c) {
            skipSpaces();
            if (i >= s.length() || s.charAt(i) != c) {
                throw new IllegalArgumentException(
                        "Invalid JSON: expected '" + c + "' at position " + i);
            }
            i++;
        }

        Map<String, Object> object() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipSpaces();
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return map;
            }
            while (true) {
                skipSpaces();
                String key = string();
                expect(':');
                map.put(key, value());
                skipSpaces();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                expect('}');
                return map;
            }
        }

        private Object value() {
            skipSpaces();
            if (i >= s.length()) {
                throw new IllegalArgumentException("Invalid JSON: unexpected end");
            }
            char c = s.charAt(i);
            if (c == '"') return string();
            if (s.startsWith("true", i))  { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i))  { i += 4; return null; }
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (start == i) {
                throw new IllegalArgumentException("Invalid JSON value at position " + i);
            }
            return Double.valueOf(s.substring(start, i));
        }

        private String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (i >= s.length()) break;
                    char n = s.charAt(i++);
                    switch (n) {
                        case 'n' -> sb.append('\n');
                        case 't' -> sb.append('\t');
                        case 'r' -> sb.append('\r');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                        }
                        default -> sb.append(n);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Invalid JSON: unterminated string");
        }
    }
}
