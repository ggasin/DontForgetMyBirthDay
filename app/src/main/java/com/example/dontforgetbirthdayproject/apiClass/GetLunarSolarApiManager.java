package com.example.dontforgetbirthdayproject.apiClass;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

//lunar는 윤달까지 포함해서 일 , 윤달여부, 월 , 년 순으로 arr에 저장됨
//solar는 일,월,년 순으로 arr에 저장됨
public class GetLunarSolarApiManager{
    //음력 받아오기
    public interface LunarCallback {
        void onSuccess(ArrayList<String> result);
        void onFailure(Exception e);
    }
    //음력 받아오기
    public interface LunarCallbackInMp {
        void onSuccess(ArrayList<String> result,String name);
        void onFailure(Exception e);
    }

    public void getLunar(String year, String month, String day, LunarCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String key="%2FFuD9koHUvAqYGTL1DrKzBYNcJcVxuraDgxxPn5TpZr%2B5m6YCmEc1Bf%2BJB1tEUn5MqMMXXxtZ6wr9ngFMyJOoQ%3D%3D";
                ArrayList<String> arr = new ArrayList<>();
                String queryUrl="http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getLunCalInfo?solYear="+year+"&solMonth="+month+"&solDay="+day+"&ServiceKey="+key;

                try{

                    URL url= new URL(queryUrl);
                    InputStream is= url.openStream();

                    XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                    XmlPullParser xpp= factory.newPullParser();
                    xpp.setInput( new InputStreamReader(is, "UTF-8") );

                    String tag;

                    xpp.next();
                    int eventType= xpp.getEventType();
                    while( eventType != XmlPullParser.END_DOCUMENT ){
                        switch( eventType ){
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                tag= xpp.getName();
                                if(tag.equals("item")) ;
                                else if(tag.equals("lunYear")){
                                    xpp.next();
                                    arr.add(xpp.getText());
                                }
                                else if(tag.equals("lunMonth")){
                                    xpp.next();
                                    arr.add(xpp.getText());
                                } else if(tag.equals("lunDay")){
                                    xpp.next();
                                    arr.add(xpp.getText());
                                } else if(tag.equals("lunLeapmonth")){
                                    xpp.next();
                                    arr.add(xpp.getText());
                                }
                                break;

                            case XmlPullParser.TEXT:
                                break;
                            case XmlPullParser.END_TAG:
                                break;
                        }

                        eventType= xpp.next();
                    }

                    callback.onSuccess(arr);

                } catch (Exception e){
                    e.printStackTrace();
                    callback.onFailure(e);
                }

            }
        }).start();

    }
    public ArrayList<String> getLunarInMp(String year, String month, String day) {
        String key="%2FFuD9koHUvAqYGTL1DrKzBYNcJcVxuraDgxxPn5TpZr%2B5m6YCmEc1Bf%2BJB1tEUn5MqMMXXxtZ6wr9ngFMyJOoQ%3D%3D";
        ArrayList<String> arr = new ArrayList<>();
        String queryUrl="http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getLunCalInfo?solYear="+year+"&solMonth="+month+"&solDay="+day+"&ServiceKey="+key;

        try{

            URL url= new URL(queryUrl);
            InputStream is= url.openStream();

            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") );

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();
                        if(tag.equals("item")) ;
                        else if(tag.equals("lunYear")){
                            xpp.next();
                            arr.add(xpp.getText());
                        }
                        else if(tag.equals("lunMonth")){
                            xpp.next();
                            arr.add(xpp.getText());
                        } else if(tag.equals("lunDay")){
                            xpp.next();
                            arr.add(xpp.getText());
                        } else if(tag.equals("lunLeapmonth")){
                            xpp.next();
                            arr.add(xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType= xpp.next();
            }



        } catch (Exception e){
            e.printStackTrace();

        }
        return arr;




    }


    //양력 받아오기기
    public ArrayList<String> getSolar(String year, String month, String day){
        String key="%2FFuD9koHUvAqYGTL1DrKzBYNcJcVxuraDgxxPn5TpZr%2B5m6YCmEc1Bf%2BJB1tEUn5MqMMXXxtZ6wr9ngFMyJOoQ%3D%3D";
        ArrayList<String> arr = new ArrayList<>();
        String queryUrl="http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getSolCalInfo?lunYear="+year+"&lunMonth="+month+"&lunDay="+day+"&ServiceKey="+key;
        try{
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is= url.openStream(); //url위치로 입력스트림 연결
            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();//xml파싱을 위한
            XmlPullParser xpp= factory.newPullParser();
            xpp.setInput( new InputStreamReader(is, "UTF-8") ); //inputstream 으로부터 xml 입력받기

            String tag;

            xpp.next();
            int eventType= xpp.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT ){
                switch( eventType ){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag= xpp.getName();//테그 이름 얻어오기
                        if(tag.equals("item")) ;// 첫번째 검색결과
                        else if(tag.equals("solYear")){
                            xpp.next();
                            arr.add(xpp.getText());//title 요소의 TEXT 읽어와서 문자열버퍼에 추가
                        }
                        else if(tag.equals("solMonth")){
                            xpp.next();
                            arr.add(xpp.getText());
                        } else if(tag.equals("solDay")){
                            xpp.next();
                            arr.add(xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType= xpp.next();
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return arr;
    }


}
