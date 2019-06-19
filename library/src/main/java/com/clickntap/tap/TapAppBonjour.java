package com.clickntap.tap;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.json.JSONArray;
import org.json.JSONObject;

public class TapAppBonjour {
    private JSONArray networkServices;
    private NsdManager.DiscoveryListener discoveryListener;
    private TapApp app;
    private boolean discovering;

    public TapAppBonjour(TapApp app) {
        this.discovering = false;
        this.app = app;
        networkServices = new JSONArray();
        discoveryListener = new NsdManager.DiscoveryListener() {
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            }

            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            }

            public void onDiscoveryStarted(String serviceType) {
            }

            public void onDiscoveryStopped(String serviceType) {
            }

            public void onServiceFound(NsdServiceInfo serviceInfo) {
                if (discovering) {
                    resolveService(serviceInfo);
                }
            }

            public void onServiceLost(NsdServiceInfo serviceInfo) {
                if (discovering) {
                    removeService(serviceInfo);
                    TapAppBonjour.this.app.notification("bonjour", getNetworkServices());
                }
            }
        };
    }

    public boolean isDiscovering() {
        return discovering;
    }

    public void resolveService(NsdServiceInfo serviceInfo) {
        NsdManager networkServiceDiscoveryManager = (NsdManager) app.getSystemService(Context.NSD_SERVICE);
        NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
            public void onResolveFailed(final NsdServiceInfo serviceInfo, int errorCode) {
                if (discovering) {
                    TapAppBonjour.this.app.setTimeout(new TapTask.Task() {
                        public void exec() throws Exception {
                            resolveService(serviceInfo);
                        }
                    }, 1000);
                }
            }

            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                if (discovering) {
                    JSONObject service = new JSONObject();
                    try {
                        service.put("name", serviceInfo.getServiceName());
                    } catch (Exception e) {
                    }
                    try {
                        service.put("host", serviceInfo.getHost().getHostAddress());
                    } catch (Exception e) {
                    }
                    try {
                        service.put("port", serviceInfo.getPort());
                    } catch (Exception e) {
                    }
                    try {
                        removeService(serviceInfo);
                        networkServices.put(service);
                    } catch (Exception e) {
                    }
                    TapAppBonjour.this.app.notification("bonjour", getNetworkServices());
                }
            }
        };
        networkServiceDiscoveryManager.resolveService(serviceInfo, resolveListener);
    }

    private void removeService(NsdServiceInfo serviceInfo) {
        try {
            JSONArray services = new JSONArray();
            for (int i = 0; i < networkServices.length(); i++) {
                JSONObject service = networkServices.getJSONObject(i);
                if (!service.getString("name").equals(serviceInfo.getServiceName())) {
                    services.put(service);
                }
            }
            networkServices = services;
        } catch (Exception e) {
        }
    }

    public JSONObject getNetworkServices() {
        JSONObject set = new JSONObject();
        try {
            set.put("what", "bonjour");
            set.put("items", networkServices);
        } catch (Exception e) {
            TapUtils.log(e);
        }
        return set;
    }

    public void startDiscovery() {
        //TapUtils.log("TapAppBonjour::startDiscovery");
        if (!discovering) {
            discovering = true;
            NsdManager networkServiceDiscoveryManager = (NsdManager) app.getSystemService(Context.NSD_SERVICE);
            networkServiceDiscoveryManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }
    }

    public void stopDiscovery() {
        //TapUtils.log("TapAppBonjour::stopDiscovery");
        if (discovering) {
            discovering = false;
            NsdManager networkServiceDiscoveryManager = (NsdManager) app.getSystemService(Context.NSD_SERVICE);
            networkServiceDiscoveryManager.stopServiceDiscovery(discoveryListener);
        }
    }

}
