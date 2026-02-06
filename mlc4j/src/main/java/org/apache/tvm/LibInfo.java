/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tvm;

import java.util.List;

class LibInfo {

    // ==========================================================
    // 1. LES VRAIES FONCTIONS NATIVES (Doivent matcher ton fichier .so)
    // ==========================================================

    native int nativeLibInit(String tvmLibFile);
    native int shutdown();

    // Nom trouvé via nm: Java_org_apache_tvm_LibInfo_tvmGetLastError
    native String tvmGetLastError();

    // Nom trouvé via nm: Java_org_apache_tvm_LibInfo_tvmFuncFree
    native int tvmFuncFree(long handle);

    native void tvmFuncPushArgLong(long arg);
    native void tvmFuncPushArgDouble(double arg);
    native void tvmFuncPushArgString(String arg);
    native void tvmFuncPushArgBytes(byte[] arg);
    native void tvmFuncPushArgHandle(long arg, int argTypeIndex);

    native int tvmFuncListGlobalNames(List<String> funcNames);

    // C'est CELUI-CI qui posait problème. On le garde en native uniquement.
    native int tvmFuncGetGlobal(String name, Base.RefLong handle);

    native int tvmFuncRegisterGlobal(String name, long handle, int override);
    native int tvmFuncCall(long handle, Base.RefTVMValue retVal);
    native int tvmFuncCreateFromCFunc(Function.Callback function, Base.RefLong handle);

    native int tvmArrayGetShape(long handle, List<Long> shape);
    native int tvmArrayCopyFromTo(long from, long to);
    native int tvmArrayCopyFromJArray(byte[] fromRaw, long to);
    native int tvmArrayCopyToJArray(long from, byte[] to);

    native int tvmSynchronize(int deviceType, int deviceId);

    // Nom trouvé via nm: Java_org_apache_tvm_LibInfo_tvmArrayAlloc
    // Note: J'ai mis private car signature complexe, mais le nom match.
    native int tvmArrayAlloc(long[] shape, int dtypeCode, int dtypeBits,
                             int dtypeLanes, int deviceType, int deviceId,
                             Base.RefLong handle);


    // ==========================================================
    // 2. LE PONT (Traduction Ancien Java -> Nouveau .so)
    // ==========================================================

    // Utilisé par Base.java
    String tvmFFIGetLastError() {
        return tvmGetLastError();
    }

    // Object
    int tvmFFIObjectFree(long handle) {
        return tvmFuncFree(handle);
    }

    // Function
    void tvmFFIFunctionPushArgLong(long arg) {
        tvmFuncPushArgLong(arg);
    }

    void tvmFFIFunctionPushArgDouble(double arg) {
        tvmFuncPushArgDouble(arg);
    }

    void tvmFFIFunctionPushArgString(String arg) {
        tvmFuncPushArgString(arg);
    }

    void tvmFFIFunctionPushArgBytes(byte[] arg) {
        tvmFuncPushArgBytes(arg);
    }

    void tvmFFIFunctionPushArgHandle(long arg, int argTypeIndex) {
        tvmFuncPushArgHandle(arg, argTypeIndex);
    }

    // Fonction manquante dans le .so, on évite le crash
    void tvmFFIFunctionPushArgDevice(Device device) {
        // Ignoré pour l'instant
    }

    int tvmFFIFunctionListGlobalNames(List<String> funcNames) {
        return tvmFuncListGlobalNames(funcNames);
    }

    // --- CORRECTION DE L'AMBIGUÏTÉ ---
    // L'ancien code appelle tvmFFI..., on le redirige vers le native tvmFunc...
    // On NE redéfinit PAS tvmFuncGetGlobal en Java, on utilise directement le native plus haut.
    int tvmFFIFunctionGetGlobal(String name, Base.RefLong handle) {
        return tvmFuncGetGlobal(name, handle);
    }

    int tvmFFIFunctionSetGlobal(String name, long handle, int override) {
        return tvmFuncRegisterGlobal(name, handle, override);
    }

    int tvmFFIFunctionCall(long handle, Base.RefTVMValue retVal) {
        return tvmFuncCall(handle, retVal);
    }

    int tvmFFIFunctionCreateFromCallback(Function.Callback function, Base.RefLong handle) {
        return tvmFuncCreateFromCFunc(function, handle);
    }

    // Tensor
    int tvmFFIDLTensorGetShape(long handle, List<Long> shape) {
        return tvmArrayGetShape(handle, shape);
    }

    int tvmFFIDLTensorCopyFromTo(long from, long to) {
        return tvmArrayCopyFromTo(from, to);
    }

    int tvmFFIDLTensorCopyFromJArray(byte[] fromRaw, long to) {
        return tvmArrayCopyFromJArray(fromRaw, to);
    }

    int tvmFFIDLTensorCopyToJArray(long from, byte[] to) {
        return tvmArrayCopyToJArray(from, to);
    }

    int tvmTensorEmpty(long[] shape, int dtypeCode, int dtypeBits,
                       int dtypeLanes, int deviceType, int deviceId,
                       Base.RefLong handle) {
        return tvmArrayAlloc(shape, dtypeCode, dtypeBits, dtypeLanes, deviceType, deviceId, handle);
    }
}