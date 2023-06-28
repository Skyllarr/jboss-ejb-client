package org.jboss.ejb.protocol.remote;

import org.jboss.ejb.client.EJBClientInterceptor;
import org.jboss.ejb.client.EJBClientInvocationContext;
import org.jboss.ejb.client.annotation.ClientInterceptorPriority;
import org.wildfly.security.auth.client.AuthenticationContext;

import static org.jboss.ejb._private.Keys.AUTHENTICATION_CONTEXT_ATTACHMENT_KEY;

@ClientInterceptorPriority(RemoteOutboundAuthenticationContextEJBClientInterceptor.PRIORITY)
public class RemoteOutboundAuthenticationContextEJBClientInterceptor implements EJBClientInterceptor {
    public static final int PRIORITY = ClientInterceptorPriority.JBOSS_AFTER + 30;

    @Override
    public void handleInvocation(EJBClientInvocationContext context) throws Exception {
        AuthenticationContext capturedCurrent = AuthenticationContext.captureCurrent();
        AuthenticationContext deploymentsAuthenticationContext = AuthenticationContext.getContextManager().getClassLoaderDefault(context.getInvokedProxy().getClass().getClassLoader());
        // non-empty configured authentication context of the thread has always precedence over everything else, no matter in which thread this context was established
        if (capturedCurrent.equals(AuthenticationContext.empty()) &&
                deploymentsAuthenticationContext != null && !deploymentsAuthenticationContext.equals(AuthenticationContext.empty())) {
            context.putAttachment(AUTHENTICATION_CONTEXT_ATTACHMENT_KEY, deploymentsAuthenticationContext);
        }
        context.sendRequest();
    }

    @Override
    public Object handleInvocationResult(EJBClientInvocationContext context) throws Exception {
        return context.getResult();
    }
}
