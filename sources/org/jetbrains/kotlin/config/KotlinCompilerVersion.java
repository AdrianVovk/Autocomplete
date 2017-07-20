/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.config;

public class KotlinCompilerVersion {
    // The value of this constant is generated by the build script
    // DON'T MODIFY IT
    public static final String VERSION = "1.1.3-2";

    // True if this compiler is of a non-stable (EAP or Beta) version.
    // Binaries produced by this compiler can not be loaded by release versions of the compiler.
    // Change this value before and after every major release
    private static final boolean IS_PRE_RELEASE = false;

    public static final String TEST_IS_PRE_RELEASE_SYSTEM_PROPERTY = "kotlin.test.is.pre.release";

    public static boolean isPreRelease() {
        String overridden = System.getProperty(TEST_IS_PRE_RELEASE_SYSTEM_PROPERTY);
        if (overridden != null) {
            return Boolean.parseBoolean(overridden);
        }

        return IS_PRE_RELEASE;
    }

    static {
        if (!VERSION.equals("1.1.3-2") && !VERSION.contains("-") && IS_PRE_RELEASE) {
            throw new IllegalStateException(
                    "IS_PRE_RELEASE cannot be true for a compiler without '-' in its version.\n" +
                    "Please change IS_PRE_RELEASE to false, commit and push this change to master"
            );
        }
    }
}