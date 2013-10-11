/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.test.cache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 멀티스레드에서 사용될 클래스
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public abstract class MultiThreadTestTool {
	private static final Logger logger = LoggerFactory.getLogger(MultiThreadTestTool.class);
	
    private static ExecutorService newExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public static void runTasks(int count, final Runnable runnable) {
        ExecutorService executor = newExecutorService();
        try {
            final CountDownLatch latch = new CountDownLatch(count);
            for (int i = 0; i < count; i++) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        latch.countDown();
                    }
                });
            }
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Exception occured.", e);
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }
}
