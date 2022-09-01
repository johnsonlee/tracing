package io.johnsonlee.tracing.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.johnsonlee.tracing.util.ClassFinder.*;

public class LooperUtil {

    public static final String PREFIX_ENTER_LOOP = ">>>>> Dispatching to ";

    public static final String PREFIX_EXIT_LOOP = "<<<<< Finished to ";

    private static final String PREFIX_HANDLER = "Handler (";

    private static final String PREFIX_DISPATCHED_CONTINUATION = "DispatchedContinuation[";

    private static final String PREFIX_CONTINUATION_AT = ", Continuation at ";

    private static final Class<?> CLASS_ANDROID_HANDLER = findClass("android.os.Handler");

    /**
     * Build a section label for tracing purpose based on the given message
     *
     * @param message the message printed by Android looper
     * @return a valid section label
     */
    public static String buildTraceLabel(final String message) {
        final String label = identity(message);
        return (label.length() < 127) ? label : label.substring(0, 127);
    }

    /**
     * Build a stack trace based on the given message
     *
     * @param message the message printed by Android looper
     * @return stack trace with handler and callback
     */
    public static StackTraceElement[] buildStackTrace(final String message) {
        final String[] names = findFullQualifiedClassNames(message, message.length() >> 2);
        final List<StackTraceElement> trace = new ArrayList<>(names.length);

        for (final String fqcn : names) {
            final Class<?> clazz = findClass(fqcn);
            if (null == clazz) continue;

            final StackTraceElement ste = buildStackTraceElement(clazz);
            if (ste == null) continue;

            trace.add(ste);
        }

        return trace.toArray(new StackTraceElement[0]);
    }

    private static StackTraceElement buildStackTraceElement(final Class<?> clazz) {
        if (null == clazz) {
            return null;
        }

        if (null != CLASS_ANDROID_HANDLER && CLASS_ANDROID_HANDLER.isAssignableFrom(clazz)) {
            return new StackTraceElement(clazz.getName(), "handleMessage", "Handler.java", -1);
        }

        if (Runnable.class.isAssignableFrom(clazz)) {
            return new StackTraceElement(clazz.getName(), "run", getFileName(clazz), -1);
        }

        return null;
    }

    /**
     * Returns the identity of the given message, it may be one of the following:
     * <ul>
     *     <li>a full-qualified class name of {@code Handler}</li>
     *     <li>a stack frame of coroutine</li>
     *     <li>a full-qualified class name of {@code Runnable}</li>
     *     <li>the message itself</li>
     * </ul>
     *
     * @param message the message printed by Android looper
     * @return the message identity
     */
    public static String identity(final String message) {
        final String callback = findCallback(message);
        if (null != callback) {
            return callback;
        }

        final String coroutine = findCoroutineStackFrame(message, 0, message.length());
        if (null != coroutine) {
            return coroutine;
        }

        final String handler = findHandler(message);
        if (null != handler) {
            return handler;
        }

        final String fqcn = findFullQualifiedClassName(message);
        final String label = null != fqcn ? fqcn : message;
        return (label.length() < 127) ? label : label.substring(0, 127);
    }

    /**
     * Retrieve the what of message
     *
     * @param message the message printed by Android looper
     * @return the what of message or 0 if not found
     */
    public static int findWhat(final String message) {
        if (!message.startsWith(PREFIX_ENTER_LOOP)) return 0;

        int n = message.length();
        int i = n - 1;

        while (i >= 0 && Character.isDigit(message.charAt(i))) {
            i--;
        }

        return i < 0 ? 0 : Integer.parseInt(message.substring(i + 1));
    }

    /**
     * Retrieve the message target handler
     *
     * @param message the message printed by Android looper
     * @return the string presentation of the message target handler or null if not found
     */
    public static String findHandler(final String message) {
        int start = indexOfHandler(message);
        if (start < 0) {
            return null;
        }

        start += PREFIX_HANDLER.length();

        final int end = message.indexOf(')', start);
        if (end < 0) {
            return null;
        }

        return message.substring(start, end);
    }

    /**
     * Retrieve the callback of message
     *
     * @param message the message printed by Android looper
     * @return the string presentation of the callback or null if not found
     */
    public static String findCallback(final String message) {
        int start = indexOfHandler(message);
        if (start < 0) {
            return null;
        }

        start = message.indexOf('}', start + PREFIX_HANDLER.length());
        if (start < 0) {
            return null;
        }

        while (' ' == message.charAt(++start)) {
            // skip spaces
        }

        // ignore null literal
        int end = '>' == message.charAt(0) ? message.indexOf(':', start) : message.length();
        if (start + 4 == end
                && message.charAt(start) == 'n'
                && message.charAt(start + 1) == 'u'
                && message.charAt(start + 2) == 'l'
                && message.charAt(start + 3) == 'l') {
            return null;
        }

        // coroutine stack frame is preferred
        final String coroutine = findCoroutineStackFrame(message, start, end);
        if (coroutine != null) {
            return coroutine;
        }

        final int at = message.lastIndexOf('@', end);
        if (start < at && at < end) {
            end = at;
        }

        return message.substring(start, end);
    }

    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[Dispatchers.Main, Continuation at com.example.Example@123456]
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[Dispatchers.Main.immediate, Continuation at com.example.Example@123456]
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[ClassSimpleName@123456, Continuation at com.example.Example@123456]
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[..., Continuation at com.example.Example@123456]
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[..., Continuation at com.example.Example.methodName(SourceFile:123)@123456]
    //
    // Example:
    //
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[Dispatchers.Default, Continuation at com.example.Example@123456]
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[Dispatcher(TestCoroutineContext@123456), Continuation at com.example.Example@123456]
    // Handler (android.app.ActivityThread$H) {123456} DispatchedContinuation[DefaultDispatcher@123456[Pool Size {core = 1, max = 2}, Worker States {CPU = 8, blocking = 0, parked = 0, dormant = 0, terminated = 0}, running workers queues = 1, global CPU queue size = 1, global blocking queue size = 1, Control State {created workers = 0, blocking tasks = 0, CPUs acquired = 0}], Continuation at com.example.Example@123456]
    private static String findCoroutineStackFrame(final String message, final int startIndex, final int endIndex) {
        int start = message.indexOf(PREFIX_DISPATCHED_CONTINUATION, startIndex);
        int end = message.lastIndexOf(']', endIndex);

        if (start < 0 || end < 0 || start > end) {
            return null;
        }

        start += PREFIX_DISPATCHED_CONTINUATION.length();

        final int ct = message.indexOf(PREFIX_CONTINUATION_AT, start);
        if (ct > -1) {
            start = ct + PREFIX_CONTINUATION_AT.length();
        } else {
            final int sp = message.lastIndexOf(' ', endIndex);
            if (sp > start) {
                start = sp + 1;
            }
        }

        final int at = message.lastIndexOf('@', end);
        if (start < at && at < end) {
            end = at;
        }

        return message.substring(start, end);
    }

    /**
     * Find the last full qualified class name in the message
     *
     * @param message the message printed by Android looper
     * @return a full qualified class name or null if not found
     */
    public static String findFullQualifiedClassName(final String message) {
        final String[] fqcn = findFullQualifiedClassNames(message, 1);
        return fqcn.length > 0 ? fqcn[0] : null;
    }

    public static String[] findFullQualifiedClassNames(final String message, int limit) {
        if (limit <= 0) {
            return new String[0];
        }

        final List<String> classes = new ArrayList<>();
        final int endIndex = message.length();

        for (int p = endIndex; p > 0; ) {
            int end = p;
            int start = p;

            // locate the end of FQCN
            while (end > 0) {
                final char c = message.charAt(end - 1);
                if (Character.isJavaIdentifierPart(c)) {
                    break;
                }
                end--;
                start--;
            }

            if (end <= 0) {
                break;
            }

            // locate the start of FQCN
            int dot = -1;

            for (int i = end - 1; i > 0; i--) {
                final char c0 = message.charAt(i - 1);
                final char c1 = message.charAt(i);

                if (c0 == '.') {
                    dot = dot < 0 ? i : dot;
                    if (!Character.isJavaIdentifierStart(c1)) {
                        break;
                    }
                }

                if (c1 != '.' && !Character.isJavaIdentifierPart(c1)) {
                    break;
                }

                start = i;
            }

            if (start < dot && dot < end) {
                classes.add(message.substring(start, end));
            }

            if (classes.size() >= limit) {
                break;
            }

            p = start - 1;
        }

        return classes.toArray(new String[0]);
    }

    private static int indexOfHandler(final String message) {
        final int c = message.charAt(0);
        return c == '>' ? message.indexOf(PREFIX_HANDLER, PREFIX_ENTER_LOOP.length())
                : c == '<' ? message.indexOf(PREFIX_HANDLER, PREFIX_EXIT_LOOP.length())
                : -1;
    }

    private LooperUtil() {
        throw new UnsupportedOperationException();
    }

}
