package com.zsj.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadPoolConf {
    private String name;
    private int core;
    private int max;
    private int queueSize;
    private String rejectPolicy;

    /**
     *  name相同就认为是相同的配置信息
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThreadPoolConf that = (ThreadPoolConf) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
