// ITestAidl.aidl
package com.zhaoxiuyuan.aidltest;

// Declare any non-default types here with import statements
import com.zhaoxiuyuan.aidltest.Person;

interface ITestAidl {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    Person modifyPerson(in Person p);
}
