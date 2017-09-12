package com.dolphin.registry.consumer;

import com.dolphin.core.exception.ServiceInfoFormatException;
import com.dolphin.core.protocle.transport.ServiceInfo;
import com.dolphin.registry.ServiceChangeListener;
import com.dolphin.registry.ServiceInfoContainer;
import com.dolphin.registry.ServiceInfoContainer.ServiceInfoSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServiceCustomer implements ServiceCustomer, ServiceChangeListener {

    private ServiceInfoContainer cachedServiceInfos = new ServiceInfoContainer();

    private Logger logger = LoggerFactory
            .getLogger(AbstractServiceCustomer.class);

    private List<ServiceChangeListener> listeners = new ArrayList<>();

    /**
     * 添加Serive改变监听器
     *
     * @param listener
     * @author keyhunter
     * 2016年5月25日 下午10:11:35
     */
    public void addServiceListener(ServiceChangeListener listener) {
        if (listener == null) {
            return;
        }
        logger.info("Listener [" + listener.getClass().getName() + "] register success.");
        listeners.add(listener);
    }

    @Override
    public ServiceInfo[] getServices(String group, String serviceName) {
        ServiceInfoSet serviceInfoSet = cachedServiceInfos.get(group, serviceName);
        if (serviceInfoSet != null && serviceInfoSet.size() > 0) {
            ServiceInfo[] serviceInfoArr = new ServiceInfo[serviceInfoSet.size()];
            return serviceInfoSet.toArray(serviceInfoArr);
        }
        ServiceInfo[] serviceInfos = getRemoteServiceInfos(group, serviceName);
        if (serviceInfos != null && serviceInfos.length > 0) {
            for (ServiceInfo serviceInfo : serviceInfos) {
                cachedServiceInfos.add(serviceInfo);
            }
        }
        return serviceInfos;
    }

    protected abstract ServiceInfo[] getRemoteServiceInfos(String group, String serviceName);

    @Override
    public void change(String group, String serviceName) {
        if (StringUtils.isBlank(group) || StringUtils.isBlank(serviceName)) {
            throw new ServiceInfoFormatException();
        }
        cachedServiceInfos.remove(group, serviceName);
        for (ServiceChangeListener listener : listeners) {
            listener.change(group, serviceName);
        }
    }

}
