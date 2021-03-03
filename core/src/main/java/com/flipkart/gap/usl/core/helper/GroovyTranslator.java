package com.flipkart.gap.usl.core.helper;

import com.codahale.metrics.Timer;
import com.flipkart.gap.usl.core.exception.GroovyException;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.google.common.base.Charsets;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static groovy.lang.GroovyShell.DEFAULT_CODE_BASE;

@Slf4j
public class GroovyTranslator {

    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private static final String GROOVY_CLASS_FORMAT = "K-%s";

    public static Class getGroovyClass(String groovyScript) throws GroovyException {
        return createGroovyClass(groovyScript);
    }

    private static String getFileName(String groovyScript) throws UnsupportedEncodingException {
        return groovyScript.length() > 50 ? Utils.getMD5(groovyScript) :URLEncoder.encode(String.format(GROOVY_CLASS_FORMAT,
                groovyScript.replace(" ","`")), Charsets.UTF_8.name()) ;
    }

    private static Class createGroovyClass(String groovyScript) throws GroovyException {
        GroovyCodeSource gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () -> {
            try {
                return new GroovyCodeSource(groovyScript, getFileName(groovyScript), DEFAULT_CODE_BASE);
            } catch (UnsupportedEncodingException e) {
                throw new GroovyException(e);
            }
        });
        return groovyClassLoader.parseClass(gcs);
    }

    public static Object translate(String groovyScript, Object data) throws GroovyException {
        try(Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(GroovyTranslator.class.getSimpleName()).time()) {
            Class groovyClass = getGroovyClass(groovyScript);
            Script script = (Script) groovyClass.newInstance();
            Binding binding = new Binding();
            binding.setVariable("data", data);
            script.setBinding(binding);
            return script.run();
        } catch (Throwable e) {
            log.debug("Error while transformation using groovy for groovyScript: " + groovyScript, e);
            throw new GroovyException("Error while transformation using groovy for groovyScript");
        }
    }
}
