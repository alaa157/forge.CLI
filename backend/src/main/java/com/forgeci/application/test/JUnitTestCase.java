package com.forgeci.application.test;
import com.forgeci.domain.test.TestStatus;
public record JUnitTestCase(String suite,String className,String name,long durationMs,TestStatus status,String failureMessage,String stdout,String stderr){}