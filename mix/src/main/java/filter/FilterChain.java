package filter;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class FilterChain {
    private int pos = 0;
    private final List<Filter> filters = new ArrayList<>();

    public void doFilter(Request request) {
        if (pos < filters.size()) {
            filters.get(pos++).doFilter(request, this);
        } else {
            System.out.println("Servlet, handle response");
        }
    }

    public void addFilter(Filter filter) {
        Assert.notNull(filter, "Filter cannot be null");

        filters.add(filter);
    }
}
