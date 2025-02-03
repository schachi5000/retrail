package pro.schacher.gpsrekorder.shared.location

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCClass
import objcnames.classes.Protocol
import platform.darwin.NSUInteger

class LocationManagerDelegate : platform.CoreLocation.CLLocationManagerDelegateProtocol {
    override fun description(): String? {
        TODO("Not yet implemented")
    }

    override fun `class`(): ObjCClass? {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun conformsToProtocol(aProtocol: Protocol?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hash(): NSUInteger {
        TODO("Not yet implemented")
    }

    override fun isEqual(`object`: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isKindOfClass(aClass: ObjCClass?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isMemberOfClass(aClass: ObjCClass?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isProxy(): Boolean {
        TODO("Not yet implemented")
    }

    @ExperimentalForeignApi
    override fun performSelector(aSelector: COpaquePointer?, withObject: Any?): Any? {
        TODO("Not yet implemented")
    }

    @ExperimentalForeignApi
    override fun performSelector(aSelector: COpaquePointer?): Any? {
        TODO("Not yet implemented")
    }

    @ExperimentalForeignApi
    override fun performSelector(
        aSelector: COpaquePointer?,
        withObject: Any?,
        _withObject: Any?
    ): Any? {
        TODO("Not yet implemented")
    }

    @ExperimentalForeignApi
    override fun respondsToSelector(aSelector: COpaquePointer?): Boolean {
        TODO("Not yet implemented")
    }

    override fun superclass(): ObjCClass? {
        TODO("Not yet implemented")
    }
}