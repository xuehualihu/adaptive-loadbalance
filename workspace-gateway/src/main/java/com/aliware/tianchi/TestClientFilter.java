package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            if (UserLoadBalance.statusMap.size() != 0 && invocation.getMethodName().equals("hash")) {
                ServerStatus serverStatus = UserLoadBalance.statusMap.get(invoker.getUrl().getPort());
                if (serverStatus != null) {
                    serverStatus.start(invocation);
                }
            }
            
            Result result = invoker.invoke(invocation);
            return result;
        }catch (Exception e){
            throw e;
        }
    }
    
    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if (UserLoadBalance.statusMap.size() != 0 && invocation.getMethodName().equals("hash")) {
            ServerStatus serverStatus = UserLoadBalance.statusMap.get(invoker.getUrl().getPort());
            if (serverStatus != null) {
                serverStatus.stop(result, invocation);
            }
        }
        return result;
    }
}
