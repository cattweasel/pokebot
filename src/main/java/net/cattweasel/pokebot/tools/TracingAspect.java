package net.cattweasel.pokebot.tools;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;

@Aspect("pertypewithin(!@net.cattweasel.pokebot.tools.Untraced net.cattweasel.pokebot..*)")
@Untraced
public class TracingAspect {

    private static final List<String> SENSITIVE_VALUES = Arrays.asList("password", "botToken");
    
    private Log log;
    
    @After("staticInit()")
    public void initLogger(JoinPoint.StaticPart jps) {
        this.log = LogFactory.getLog(jps.getSignature().getDeclaringTypeName());
    }
    
    @Before("tracedConstructors()")
    public void traceConstructorEntry(JoinPoint jp) {
        if (log != null && log.isTraceEnabled()) {
            log.trace(entryMsg(jp));
        }
    }
    
    @AfterReturning("tracedConstructors()")
    public void traceConstructorExit(JoinPoint jp) {
        if (log != null && log.isTraceEnabled()) {
            log.trace(exitMsg(jp, null));
        }
    }
    
    @AfterThrowing(pointcut="tracedConstructors()", throwing="t")
    public void traceConstructorThrow(JoinPoint jp, Throwable t) {
        if (log != null && log.isTraceEnabled()) {
            log.trace(throwingMsg(jp, t));
        }
    }
    
    @Before("tracedMethods()")
    public void traceMethodEntry(JoinPoint jp) {
        if (log != null && log.isTraceEnabled()) {
            log.trace(entryMsg(jp));
        }
    }
    
    @AfterReturning(pointcut="tracedMethods()", returning="r")
    public void traceMethodExit(JoinPoint jp, Object r) {
        if (log != null && log.isTraceEnabled()) {
            log.trace(exitMsg(jp, r));
        }
    }
    
    @AfterThrowing(pointcut="tracedMethods()", throwing="t")
    public void traceMethodThrow(JoinPoint jp, Throwable t) {
        if (log != null && log.isTraceEnabled()) {
            log.trace(throwingMsg(jp, t));
        }
    }
    
    @Pointcut("staticinitialization(*)")
    public void staticInit() {}
    
    @Pointcut("execution(new(..))")
    public void tracedConstructors() {}
    
    @Pointcut("execution(* *(..)) && !execution(String toString()) && !execution(* access$*(..))")
    public void tracedMethods() {}
    
    private String entryMsg(JoinPoint jp) {
        StringBuilder sb = new StringBuilder("Entering ");
        CodeSignature sig = (CodeSignature) jp.getSignature();
        sb.append(sig.getName()).append("(");
        String[] params = sig.getParameterNames();
        Object[] paramVals = jp.getArgs();
        int startParam = isConstructorOfInnerClass(jp) ? 1 : 0;
        for (int i=startParam; i<params.length; i++) {
            sb.append(params[i]).append(" = ");
            sb.append(isSensitive(params[i]) ? "*****" : filterValue(paramVals[i]));
            if (i != params.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    private String exitMsg(JoinPoint jp, Object obj) {
        StringBuilder sb = new StringBuilder("Exiting ");
        sb.append(jp.getSignature().getName()).append(" = ").append(filterValue(obj));
        return sb.toString();
    }
    
    private String throwingMsg(JoinPoint jp, Throwable t) {
        StringBuilder sb = new StringBuilder("Throwing ");
        sb.append(jp.getSignature().getName()).append(" - ").append(t.toString());
        return sb.toString();
    }
    
    private String filterValue(Object value) {
        if (value instanceof Map) {
            return filterMap((Map<?, ?>) value);
        }
        if (isSensitive(value)) {
            return "*****";
        }
        return String.valueOf(value);
    }
    
    private String filterMap(Map<?, ?> map) {
        assert(map != null);
        Map<Object, Object> result = new HashMap<>();
        ArrayList<Object> keys = new ArrayList<>();
        for (Object key : map.keySet()) {
            keys.add(key);
        }
        for (Object key : keys) {
            if (isSensitive(key)) {
                result.put(key, "*****");
            } else {
                result.put(key, filterValue(map.get(key)));
            }
        }
        return result.toString();
    }
    
    private boolean isSensitive(Object value) {
        String str = String.valueOf(value);
        return containsAny(str, SENSITIVE_VALUES);
    }
    
    private boolean containsAny(String str, List<String> values) {
        if (str == null || values == null) {
            return false;
        }
        String lower = str.toLowerCase();
        for (String value : values) {
            String lowerValue = value.toLowerCase();
            if (lower.contains(lowerValue)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isConstructorOfInnerClass(JoinPoint jp) {
        String kind = jp.getKind();
        if (!"constructor-call".equals(kind) && !"constructor-execution".equals(kind)) {
            return false;
        }
        Signature sig = jp.getSignature();
        Class<?> declaringType = sig.getDeclaringType();
        int modifiers = declaringType.getModifiers();
        boolean isStatic = Modifier.isStatic(modifiers);
        return !isStatic && declaringType.getEnclosingClass() != null;
    }
}
