package org.sysunit.command.test;

public class SetUpTBeansCommand
    extends TestCommand {

    public SetUpTBeansCommand() {

    }

    public void run(TestServer testServer)
        throws Exception {
        testServer.setUpTBeans();
    }
}
