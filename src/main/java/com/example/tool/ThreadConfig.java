package com.example.tool;

import com.example.tool.common.model.MyHttpServletRequest;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableAsync
public class ThreadConfig implements AsyncConfigurer {

	public class ContextAwareCallable<T> implements Callable<T> {
		private Callable<T> task;
		private RequestAttributes context;

		public ContextAwareCallable(Callable<T> task, ServletRequestAttributes context) {
			this.task = task;
			this.context = new ServletRequestAttributes(new MyHttpServletRequest(context.getRequest()), context.getResponse());;
		}

		@Override
		public T call() throws Exception {
			if (context != null) {
				RequestContextHolder.setRequestAttributes(context);
			}
			try {
				return task.call();
			} finally {
				RequestContextHolder.resetRequestAttributes();
			}
		}
	}

	public class ContextAwarePoolExecutor extends ThreadPoolTaskExecutor {
		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return super.submit(new ContextAwareCallable(task, (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()));
		}

		@Override
		public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
			return super.submitListenable(new ContextAwareCallable(task, (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()));
		}
	}
	@Override
	@Bean("Async")
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ContextAwarePoolExecutor();
		// 核心线程池数量，方法；
		executor.setCorePoolSize(10);
		// 最大线程数量
		executor.setMaxPoolSize(20);
		// 线程池的队列容量
		executor.setQueueCapacity(1000);
		// 线程名称的前缀
		executor.setThreadNamePrefix("fyk-executor-");
		// 线程池对拒绝任务的处理策略
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new SimpleAsyncUncaughtExceptionHandler();
	}
}
