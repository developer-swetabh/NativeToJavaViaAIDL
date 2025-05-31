#include "DrLocationService.h"
#include <android-base/logging.h>
#include <chrono>
#include <thread>

using namespace std::chrono_literals;

DrLocationService::DrLocationService() : running_(true), intervalMs_(1500) {
    emitterThread_ = std::thread(&DrLocationService::locationEmitter, this);
}

DrLocationService::~DrLocationService() {
    running_ = false;
    if (emitterThread_.joinable()) {
        emitterThread_.join();
    }
}

ndk::ScopedAStatus DrLocationService::registerCallback(const std::shared_ptr<IDrLocationCallback>& cb) {
    std::lock_guard<std::mutex> lock(callbackMutex_);
    callback_ = cb;
    LOG(INFO) << "DrLocationService: Callback registered.";
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus DrLocationService::unregisterCallback(const std::shared_ptr<IDrLocationCallback>& cb) {
    std::lock_guard<std::mutex> lock(callbackMutex_);
    if (callback_ == cb) {
        callback_.reset();
        LOG(INFO) << "DrLocationService: Callback unregistered.";
    }
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus DrLocationService::setUpdateInterval(int milliseconds) {
    intervalMs_ = milliseconds;
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus DrLocationService::getUpdateInterval(int* _aidl_return) {
    *_aidl_return = intervalMs_;
    return ndk::ScopedAStatus::ok();
}

void DrLocationService::locationEmitter() {
    double lat = 37.4219999;
    double lon = -122.0840575;

    while (running_) {
        {
            std::lock_guard<std::mutex> lock(callbackMutex_);
            if (callback_) {
                callback_->onLocationUpdate(lat, lon);
                LOG(INFO) << "Emitted lat: " << lat << ", lon: " << lon;
                lat += 0.0001; // simulate slight movement
                lon += 0.0001;
            }
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(intervalMs_));
    }
}
