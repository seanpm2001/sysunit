package org.sysunit.local;

import org.sysunit.SystemTestCase;
import org.sysunit.SysUnitTestCase;
import org.sysunit.SingleTBeanCase;
import org.sysunit.DoubleTBeanCase;
import org.sysunit.MockTBean;
import org.sysunit.SleepTBean;
import org.sysunit.MockSynchronizableTBean;
import org.sysunit.WatchdogException;

import junit.framework.TestResult;

import java.util.Date;

public class TestLocalTBeanManager
    extends SysUnitTestCase {

    public void testConstruct()
        throws Exception {
        LocalTBeanManager manager = new LocalTBeanManager();

        manager.initialize();

        assertEmpty( manager.getTBeanThreads() );
        assertEmpty( manager.getTBeans() );
    }

    public void testStartTBeans_NotSynchronizable()
        throws Throwable {

        final MockTBean tbean = new MockTBean();

        SingleTBeanCase testCase = new SingleTBeanCase( tbean );

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        manager.startTBeans( testCase,
                             testResult );

        Thread.sleep( 2000 );

        assertTrue( tbean.hasRun() );

        assertEmpty( manager.getSynchronizer().getRegisteredTBeans() );
    }

    public void testStartTBeans_Synchronizable()
        throws Throwable {

        final MockSynchronizableTBean tbean = new MockSynchronizableTBean();

        SingleTBeanCase testCase = new SingleTBeanCase( tbean );

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        manager.startTBeans( testCase,
                             testResult );

        Thread.sleep( 2000 );

        assertTrue( tbean.hasRun() );

        assertLength( 1,
                      manager.getSynchronizer().getRegisteredTBeans() );

        assertContainsEquals( "One",
                              manager.getSynchronizer().getRegisteredTBeans() );
    }

    public void testWaitForTBeans_Single_NoTimeout()
        throws Throwable {

        final SleepTBean tbean = new SleepTBean( 5000 );

        SingleTBeanCase testCase = new SingleTBeanCase( tbean );

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        long start = new Date().getTime();

        manager.startTBeans( testCase,
                             testResult );

        manager.waitForTBeans( testCase,
                               0 );

        long stop = new Date().getTime();

        long runTime = stop - start;

        assertTrue( runTime >= 5000 );
        assertTrue( runTime < 8000 );

        assertTrue( tbean.hasRun() );
    }

    public void testWaitForTBeans_Double_NoTimeout()
        throws Throwable {

        final SleepTBean tbeanOne = new SleepTBean( 5000 );
        final SleepTBean tbeanTwo = new SleepTBean( 5000 );

        DoubleTBeanCase testCase = new DoubleTBeanCase( tbeanOne,
                                                        tbeanTwo );

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        long start = new Date().getTime();

        manager.startTBeans( testCase,
                             testResult );

        manager.waitForTBeans( testCase,
                               0 );

        long stop = new Date().getTime();

        long runTime = stop - start;

        assertTrue( runTime >= 5000 );
        assertTrue( runTime < 6000 );

        assertTrue( tbeanOne.hasRun() );
        assertTrue( tbeanTwo.hasRun() );
    }

    public void testWaitForTBeans_Double_WithTimeout_NoExpiration()
        throws Throwable {
        final SleepTBean tbeanOne = new SleepTBean( 5000 );
        final SleepTBean tbeanTwo = new SleepTBean( 1000 );

        DoubleTBeanCase testCase = new DoubleTBeanCase( tbeanOne,
                                                        tbeanTwo );

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        long start = new Date().getTime();

        manager.startTBeans( testCase,
                             testResult );

        manager.waitForTBeans( testCase,
                               6000 );

        long stop = new Date().getTime();

        long runTime = stop - start;

        assertTrue( runTime >= 5000 );
        assertTrue( runTime < 6000 );

        assertTrue( tbeanOne.hasRun() );
        assertTrue( tbeanTwo.hasRun() );
    }

    public void testWaitForTBeans_Single_WithTimeout_Expiration()
        throws Throwable {

        final SleepTBean tbeanOne = new SleepTBean( 5000 );

        SingleTBeanCase testCase = new SingleTBeanCase( tbeanOne );

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        manager.startTBeans( testCase,
                             testResult );

        try {
            manager.waitForTBeans( testCase,
                                   3000 );
            fail( "should have thrown WatchdogException" );
        } catch (WatchdogException e) {
            assertEquals( 3000,
                          e.getTimeout() );

            assertLength( 1,
                          e.getTBeanIds() );

            assertContainsEquals( "One",
                                  e.getTBeanIds() );
        }
    }

    public void testWaitForTBeans_Double_WithTimeout_SingleExpiration()
        throws Throwable {

        final SleepTBean tbeanOne = new SleepTBean( 5000 );
        final SleepTBean tbeanTwo = new SleepTBean( 50 );

        DoubleTBeanCase testCase = new DoubleTBeanCase( tbeanOne,
                                                        tbeanTwo);

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        manager.startTBeans( testCase,
                             testResult );

        try {
            manager.waitForTBeans( testCase,
                                   3000 );
            fail( "should have thrown WatchdogException" );
        } catch (WatchdogException e) {
            assertEquals( 3000,
                          e.getTimeout() );

            assertLength( 1,
                          e.getTBeanIds() );

            assertContainsEquals( "One",
                                  e.getTBeanIds() );
        }
    }

    public void testWaitForTBeans_Double_WithTimeout_DoubleExpiration()
        throws Throwable {

        final SleepTBean tbeanOne = new SleepTBean( 5000 );
        final SleepTBean tbeanTwo = new SleepTBean( 5000 );

        DoubleTBeanCase testCase = new DoubleTBeanCase( tbeanOne,
                                                        tbeanTwo);

        testCase.initializeFactories();

        LocalTBeanManager manager = new LocalTBeanManager();

        TestResult testResult = new TestResult();

        manager.startTBeans( testCase,
                             testResult );

        try {
            manager.waitForTBeans( testCase,
                                   3000 );
            fail( "should have thrown WatchdogException" );
        } catch (WatchdogException e) {
            assertEquals( 3000,
                          e.getTimeout() );

            assertLength( 2,
                          e.getTBeanIds() );

            assertContainsEquals( "One",
                                  e.getTBeanIds() );

            assertContainsEquals( "Two",
                                  e.getTBeanIds() );
        }
    }
}
