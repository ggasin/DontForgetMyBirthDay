package com.dfmbd.dontforgetbirthdayproject.calenderDecorators;

import android.content.Context;

import com.dfmbd.dontforgetbirthdayproject.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

//빨간 점 찍기
public class EventDecorator implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;



    public EventDecorator(int color, Collection<CalendarDay> dates) {

        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(7, color)); // 날자밑에 점
    }
}
