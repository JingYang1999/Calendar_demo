package cn.jingyang.calendar;


public class date_event {
    public int[] date=new int[3];
    public String info;
    public void init(int yy, int mm, int dd, String ii){
        date[0]=yy;
        date[1]=mm;
        date[2]=dd;
        info=ii;
    }
}