package com.stone.studio.sandbox;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.NameRegexFilter;
import com.alibaba.jvm.sandbox.api.http.Http;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.stone.studio.sandbox.util.ReportInfo;

import javax.annotation.Resource;
import java.util.Arrays;

@Information(id = "rce", isActiveOnLoad = false, version = "1.0.0", author = "Stone")
public class RceModule extends CommonModule {

    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Http("/report-rce")
    public void reportRce() {

        moduleEventWatcher.watch(

                // 匹配到Clock$BrokenClock#checkState()
                new NameRegexFilter("java.lang.ProcessImpl", "start"),

                // 监听THROWS事件并且改变原有方法抛出异常为正常返回
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        if(event.type == Event.Type.BEFORE) {
                            final BeforeEvent beforeEvent = (BeforeEvent)event;
                            String className = beforeEvent.javaClassName;
                            String methodName = beforeEvent.javaMethodName;
                            try {
                                double startTime = getStartTime();
                                detect("info", "cmd", className, methodName,
                                        Arrays.toString((String[])beforeEvent.argumentArray[0]), null);

                                recordExecuteTime(System.currentTimeMillis() - startTime);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        } else if(event.type == Event.Type.RETURN || event.type == Event.Type.THROWS){
                            // 显式清理thread local, 否则rasp plugin资源不能释放
                            reportInfoThreadLocal.remove();
                        }
                    }
                },

                // 指定监听的事件为抛出异常
                Event.Type.BEFORE
        );

    }
}
