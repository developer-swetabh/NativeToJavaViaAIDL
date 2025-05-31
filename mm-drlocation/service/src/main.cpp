#include "DrLocationService.h"
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <android-base/logging.h>
#include <iostream>

int main() {
    ABinderProcess_setThreadPoolMaxThreadCount(1);
    std::shared_ptr<DrLocationService> service = ndk::SharedRefBase::make<DrLocationService>();

    const std::string instance = std::string(DrLocationService::descriptor) + "/default";
    LOG(INFO) << "DrLocationService name = " << instance;
    binder_status_t status = AServiceManager_addService(service->asBinder().get(), instance.c_str());

    if (status != STATUS_OK) {
        LOG(INFO) << "Failed to register service";
        return 1;
    }

    ABinderProcess_joinThreadPool();
    return 0;
}
