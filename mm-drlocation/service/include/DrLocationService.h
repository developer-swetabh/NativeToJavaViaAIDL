#ifndef DRLOCATIONSERVICE_H
#define DRLOCATIONSERVICE_H

//#include <aidl/com/marelli/drlocation/IDrLocationService.h>
#include <aidl/com/marelli/drlocation/BnDrLocationService.h>
#include <aidl/com/marelli/drlocation/IDrLocationCallback.h>
#include <mutex>
#include <thread>
#include <atomic>

using aidl::com::marelli::drlocation::BnDrLocationService;
using aidl::com::marelli::drlocation::IDrLocationCallback;

class DrLocationService : public BnDrLocationService{
public:
    DrLocationService();
    ~DrLocationService();

    virtual ndk::ScopedAStatus registerCallback(const std::shared_ptr<IDrLocationCallback>& cb) override;
    virtual ndk::ScopedAStatus unregisterCallback(const std::shared_ptr<IDrLocationCallback>& cb) override;
    virtual ndk::ScopedAStatus setUpdateInterval(int milliseconds) override;
    virtual ndk::ScopedAStatus getUpdateInterval(int* _aidl_return) override;

private:
    void locationEmitter();

    std::shared_ptr<IDrLocationCallback> callback_;
    std::thread emitterThread_;
    std::mutex callbackMutex_;
    std::atomic<bool> running_;
    std::atomic<int> intervalMs_;
};

#endif // DRLOCATIONSERVICE_H
