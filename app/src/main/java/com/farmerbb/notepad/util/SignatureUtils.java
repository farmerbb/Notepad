/* Copyright 2018 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farmerbb.notepad.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

public class SignatureUtils {

    private SignatureUtils() {}

    public enum ReleaseType { PLAY_STORE, AMAZON, F_DROID, UNKNOWN }

    private static final String PLAY_STORE_SIGNATURE =
            "MIICzzCCAbegAwIBAgIEbCYYEjANBgkqhkiG9w0BAQsFADAYMRYwFAYDVQQDEw1C" +
            "cmFkZW4gRmFybWVyMB4XDTE0MDIxMDAxMzcwMloXDTM5MDIwNDAxMzcwMlowGDEW" +
            "MBQGA1UEAxMNQnJhZGVuIEZhcm1lcjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC" +
            "AQoCggEBAMHjcdZyi/tkMl8i/AesTzdSFWG2TeohWYk6x3FEeL/6iCccl9v74Z9f" +
            "WkOiXuzgLEAfNUsXxLnsLu93yRdc6elYEt4a0UoDc0wvysS081s7lHgaDecJM3DI" +
            "98H92yKPALvUgce9xvBEiobbzQv1IlTi5ZAms8eWn/c9x9LSTJNeyaV8yqaZfuI5" +
            "Wy9gECQcty45NjeG2Zkg3iGR4ETU8RuznNDkjC5O/MPWH9bTskTdtSp06r6tqhzm" +
            "bEnV7i1GyPGPEa2nHz0mm73foNSH/Me2/q/lekTXy1Cu3f9KFW8dYH0QbFZCu4km" +
            "E/tvmy9ArFQjgVLlEai1yEUfKt7KcYUCAwEAAaMhMB8wHQYDVR0OBBYEFLyvWiZM" +
            "NJLbdyPfhRpKlvL+4xJTMA0GCSqGSIb3DQEBCwUAA4IBAQAIA5ugmKUvZicVd/BB" +
            "+r7J5MDuGogoIYeQJpkxS2KIM7KmFk4+FC889xW6YCEoQyrPSomNxBH+moiVHUg1" +
            "NtlKMoFIdEeAqf3hdniUv5zFxvAxYoQnhfBCSzfBbIhDn7woZZOzyym72ygqE7z5" +
            "/IGa8ATW0rXPwovMiSJFQkaewgNinzNcL39Az1b1rcCC1jo42DWPhbG3mN/3hFbw" +
            "NQc6cw6UXHOYIo90rXsjr7EI4R/5zqvbSFUT9FNPUNDitZt9yUUWYxwn6qwai41c" +
            "SmARQ7nrTwuRdgyBoADiFVeVTvePnrUBLIa6UJiGRabxhEUYKl1ldk+0VrTn4ON3" +
            "73BA";

    private static final String AMAZON_SIGNATURE =
            "MIICrDCCAZSgAwIBAgIEU5um3DANBgkqhkiG9w0BAQUFADAYMRYwFAYDVQQDEw1G" +
            "YXJtZXIgQnJhZGVuMB4XDTE0MDYxNDAxMzUyNFoXDTQxMTAzMDAxMzUyNFowGDEW" +
            "MBQGA1UEAxMNRmFybWVyIEJyYWRlbjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC" +
            "AQoCggEBAOWByPZRzt17y2lQAiABH9C91xfxxd9S0lt12d+36ttACcgv2K5fcohR" +
            "8gMSMQ9Tz6VEVLE82eo0kw19fXmMUGdBdFv+hQSiz/q2C3sFIhtfK2AxM6h+2LVd" +
            "wmbYUOlzLWf2WlirizSszj7kYIjOUKkp7ISUtEbNwsFDl/6ExgBvaq2Iz+a313um" +
            "DkQRo+/VX/G+YprtwiSU0lCGkQLLOrKTvspDX4QMB32Pmvi7Pmk4Npt2ggkC4vjt" +
            "zag5TnDZLrUWAN3hyP35MnoY5R8NoNh90ClNDV1ceaDeVbc0p+DRAMrAAGZyEaSg" +
            "lbaDNXzIVYIANBUjkfSyvxuEc558zeECAwEAATANBgkqhkiG9w0BAQUFAAOCAQEA" +
            "d6Q5JZuNjsoZC0FQBCwm9qX/09AGFfFg9LEy6lX7e/ZjdXbhuMvByuOpTAmsvUt1" +
            "Zz7d3vr8oTVl1wVXTZgIyCn7B2ivwHHYTwgdvOVmr/lxod+pk/mH3tI2PYQ7YUD3" +
            "cqWltLt6NOJ1yc44jvl9Hx1qZZagg9o76uHW4+gyDMaPqgVyaCJsyeRabUwR4l+y" +
            "0XimEvDZ6dTj4LW6c7KTEFHQust3xj3qk42n+9+eLPU8SkqP4BhvR0YRf3dDUUj8" +
            "gl8W6mJnxY2wg7BnEcx2ySDObu7DwUWF8FVANI0feqUAfd4onqUJ6W+XoYMpTjdR" +
            "u1ADuR2pOPbwsdIXTsDABA==";

    private static final String F_DROID_SIGNATURE =
            "MIIDXzCCAkegAwIBAgIEQa3NRjANBgkqhkiG9w0BAQsFADBgMQswCQYDVQQGEwJV" +
            "SzEMMAoGA1UECBMDT1JHMQwwCgYDVQQHEwNPUkcxEzARBgNVBAoTCmZkcm9pZC5v" +
            "cmcxDzANBgNVBAsTBkZEcm9pZDEPMA0GA1UEAxMGRkRyb2lkMB4XDTE4MDUyMTA3" +
            "NTQ0MloXDTQ1MTAwNjA3NTQ0MlowYDELMAkGA1UEBhMCVUsxDDAKBgNVBAgTA09S" +
            "RzEMMAoGA1UEBxMDT1JHMRMwEQYDVQQKEwpmZHJvaWQub3JnMQ8wDQYDVQQLEwZG" +
            "RHJvaWQxDzANBgNVBAMTBkZEcm9pZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC" +
            "AQoCggEBAJyo0/30+Y273MfvRbnOjYdiOY6/Xq3EjsfE75cxR2yFcGjAPzYQcdm6" +
            "kTS4RazEFBr34QYNMtUw0SzIqixgXMJYNAZttsiAW4MmaLofOV772BJ6ShnNrNLY" +
            "J8NlCz9F3s1F3JtOJq2ih+SuF15Lyy8Jb/U7x+ow75rQBGFEwBnmIY1yqFFK7CJh" +
            "G660bbP9xt/0H3KOjD4jDaNGGtpATHbiH+vCy83WlkgL5TbHnrNmd99u07xEGvr7" +
            "CvlsTn2xP/9c9JGVh8NAahQhp3Bo9GfEA4ST5cCRP/W27pU3T0z/6PI2GarOPS3o" +
            "LxFcKGBY2+sKua7uZTvv2xFLadeWIfcCAwEAAaMhMB8wHQYDVR0OBBYEFPojk/dO" +
            "sld/d9pnK0xJtVBv/k6/MA0GCSqGSIb3DQEBCwUAA4IBAQAQM2vLBHb73hmuuzD1" +
            "Jo6L8YASsE54ROoKCz7zlHVkbpMfufA+lIDNaHExDyarU5kXnO15Tx0Edm+BTRoc" +
            "3FhKQrDJTyw/+auVUkk3iYGGQ1wCAiX2xq6eLXV33Gxf8uKRoaw2053eS7w5+sth" +
            "WKjZSP3xbsflw8Jx7ap7YFOuKSJOHZOR01BV2NiqIb+KwprCHmvITeIa2wYYCg0y" +
            "p6lpbWJI25re629MXhb0kshFKIrooYgXfgtMrD23rNrjD+SyYh7ejZmad5v2yPgP" +
            "TZLlHejqaAefv50O7j0llPJ3A31GOTccZDpucBYvwzmoFhdpXJeiGrWTaY+OuLDR" +
            "AYXa";

    @SuppressLint("PackageManagerGetSignatures")
    public static ReleaseType getReleaseType(Context context) {
        try {
            Signature playStore = new Signature(Base64.decode(PLAY_STORE_SIGNATURE, Base64.DEFAULT));
            Signature amazon = new Signature(Base64.decode(AMAZON_SIGNATURE, Base64.DEFAULT));
            Signature fDroid = new Signature(Base64.decode(F_DROID_SIGNATURE, Base64.DEFAULT));

            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures) {
                if(signature.equals(playStore))
                    return ReleaseType.PLAY_STORE;

                if(signature.equals(amazon))
                    return ReleaseType.AMAZON;

                if(signature.equals(fDroid))
                    return ReleaseType.F_DROID;
            }
        } catch (Exception e) { /* Gracefully fail */ }

        return ReleaseType.UNKNOWN;
    }
}