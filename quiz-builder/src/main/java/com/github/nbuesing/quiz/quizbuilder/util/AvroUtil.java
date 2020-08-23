package com.github.nbuesing.quiz.quizbuilder.util;

import java.util.List;
import java.util.Objects;
import org.apache.avro.generic.GenericData.Array;

public final class AvroUtil {
    private AvroUtil() {
    }

    public static <T> void remove(List<T> list, T item) {
        if (list != null) {
            if (list instanceof Array) {
                if (list.contains(item)) {
                    Array array = (Array) list;

                    for(int i = 0; i < array.size(); ++i) {
                        if (Objects.equals(array.get(i), item)) {
                            array.remove(i);
                            break;
                        }
                    }
                }
            } else {
                list.remove(item);
            }

        }
    }
}