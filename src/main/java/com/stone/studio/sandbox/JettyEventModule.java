package com.stone.studio.sandbox;

import com.alibaba.jvm.sandbox.api.Information;
import com.alibaba.jvm.sandbox.api.event.BeforeEvent;
import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.NameRegexFilter;
import com.alibaba.jvm.sandbox.api.http.Http;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.resource.ModuleEventWatcher;
import com.stone.studio.sandbox.filter.NameRegexExtFilter;
import com.stone.studio.sandbox.util.ReportInfo;

import javax.annotation.Resource;

@Information(id = "jetty-event", isActiveOnLoad = false, version = "1.0.0", author = "Stone")
public class JettyEventModule extends CommonModule {
    @Resource
    private ModuleEventWatcher moduleEventWatcher;

    @Http("/req")
    public void getJettyRequest() {

        moduleEventWatcher.watch(

                // 匹配到Clock$BrokenClock#checkState()
                new NameRegexFilter("org.eclipse.jetty.server.handler.HandlerWrapper", "handle"),

                // 监听THROWS事件并且改变原有方法抛出异常为正常返回
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        if(event.type == Event.Type.BEFORE) {
                            final BeforeEvent beforeEvent = (BeforeEvent)event;
                            try {
                                double startTime = getStartTime();

                                final Object request = beforeEvent.argumentArray[2]; // HttpServletRequest

                                ReportInfo reportInfo = reportInfoThreadLocal.get();
                                if (reportInfo != null) {
                                    reportInfo.setRequest(request);
                                }
                                recordExecuteTime(System.currentTimeMillis() - startTime);

                            } catch (Throwable e) {
                                // ignore
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

    @Http("/para")
    public void getJettyParameter() {

        moduleEventWatcher.watch(

                // 匹配到Clock$BrokenClock#checkState()
                new NameRegexExtFilter("org.eclipse.jetty.server.HttpInput", "read", 3),

                // 监听THROWS事件并且改变原有方法抛出异常为正常返回
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Throwable {
                        if(event.type == Event.Type.BEFORE) {
                            final BeforeEvent beforeEvent = (BeforeEvent)event;
                            try {
                                double startTime = getStartTime();
                                ReportInfo raspInfo = reportInfoThreadLocal.get();
                                if (raspInfo != null) {
                                    raspInfo.setParameters(beforeEvent.argumentArray[0]);
                                }
                                recordExecuteTime(System.currentTimeMillis() - startTime);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },

                // 指定监听的事件为抛出异常
                Event.Type.BEFORE
        );
    }
}
