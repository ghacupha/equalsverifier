/*
 * Copyright 2012, 2015 Jan Ouwens
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
package nl.jqno.equalsverifier.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the values of static member fields so they can be restored later.
 *
 * @author Jan Ouwens
 */
public class StaticFieldValueStash {
    private final Map<Class<?>, Map<Field, Object>> stash = new HashMap<>();

    /**
     * Stores the values of all static members fields of the given type.
     *
     * @param type The type for which to store the values of its static fields.
     */
    public void backup(Class<?> type) {
        for (Class<?> c : SuperclassIterable.of(type)) {
            backup(c);
        }

        if (stash.containsKey(type)) {
            return;
        }

        stash.put(type, new HashMap<Field, Object>());
        ObjectAccessor<?> objectAccessor = ObjectAccessor.of(null, type);
        for (Field field : FieldIterable.of(type)) {
            FieldAccessor accessor = objectAccessor.fieldAccessorFor(field);
            if (accessor.fieldIsStatic()) {
                stash.get(type).put(field, accessor.get());
            }
        }
    }

    /**
     * Restores the previously stored values of static fields, for all types
     * for which they were stored at once.
     */
    public void restoreAll() {
        for (Map.Entry<Class<?>, Map<Field, Object>> entry : stash.entrySet()) {
            Class<?> type = entry.getKey();
            ObjectAccessor<?> objectAccessor = ObjectAccessor.of(null, type);
            for (Field field : FieldIterable.of(type)) {
                FieldAccessor accessor = objectAccessor.fieldAccessorFor(field);
                if (accessor.fieldIsStatic()) {
                    accessor.set(entry.getValue().get(field));
                }
            }
        }
    }
}