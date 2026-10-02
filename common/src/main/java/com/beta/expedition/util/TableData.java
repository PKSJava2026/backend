package com.beta.expedition.util;

import java.util.List;

public record TableData(String name, List<String> columns, List<List<Object>> rows) {
}
