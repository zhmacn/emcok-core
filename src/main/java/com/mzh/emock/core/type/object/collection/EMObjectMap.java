package com.mzh.emock.core.type.object.collection;


import java.util.*;

public class EMObjectMap<K,V> extends HashMap<K,V> {

    public static class EMHashSet{
        public void clear(){
            this.arr=new long[4][][][][][][][][];
        }
        private long[][][][][][][][][] arr=new long[4][][][][][][][][];
        public boolean contains(int i){
            int y1=(i & 0xC0000000)>>>30;
            int y2=(i & 0xE0000000>>>2)>>>27;
            int y3=(i & 0xE0000000>>>5)>>>24;
            int y4=(i & 0xE0000000>>>8)>>>21;
            int y5=(i & 0xE0000000>>>11)>>>18;
            int y6=(i & 0xE0000000>>>14)>>>15;
            int y7=(i & 0xE0000000>>>17)>>>12;
            int y8=(i & 0xE0000000>>>20)>>>9;
            int y9=(i & 0xE0000000>>>23)>>>6;
            if(arr[y1]==null){
                return false;
            }
            if(arr[y1][y2]==null){
                return false;
            }
            if(arr[y1][y2][y3]==null){
                return false;
            }
            if(arr[y1][y2][y3][y4]==null){
                return false;
            }
            if(arr[y1][y2][y3][y4][y5]==null){
                return false;
            }
            if(arr[y1][y2][y3][y4][y5][y6]==null){
                return false;
            }
            if(arr[y1][y2][y3][y4][y5][y6][y7]==null){
                return false;
            }
            if(arr[y1][y2][y3][y4][y5][y6][y7][y8]==null){
                return false;
            }
            return (arr[y1][y2][y3][y4][y5][y6][y7][y8][y9] & (1L<<(i & 0x3F)))!=0;

        }
        public void add(int i){
            int y1=(i & 0xC0000000)>>>30;
            int y2=(i & 0xE0000000>>>2)>>>27;
            int y3=(i & 0xE0000000>>>5)>>>24;
            int y4=(i & 0xE0000000>>>8)>>>21;
            int y5=(i & 0xE0000000>>>11)>>>18;
            int y6=(i & 0xE0000000>>>14)>>>15;
            int y7=(i & 0xE0000000>>>17)>>>12;
            int y8=(i & 0xE0000000>>>20)>>>9;
            int y9=(i & 0xE0000000>>>23)>>>6;
            if(arr[y1]==null){
                arr[y1]=new long[8][][][][][][][];
            }
            if(arr[y1][y2]==null){
                arr[y1][y2]=new long[8][][][][][][];
            }
            if(arr[y1][y2][y3]==null){
                arr[y1][y2][y3]=new long[8][][][][][];
            }
            if(arr[y1][y2][y3][y4]==null){
                arr[y1][y2][y3][y4]=new long[8][][][][];
            }
            if(arr[y1][y2][y3][y4][y5]==null){
                arr[y1][y2][y3][y4][y5]=new long[8][][][];
            }
            if(arr[y1][y2][y3][y4][y5][y6]==null){
                arr[y1][y2][y3][y4][y5][y6]=new long[8][][];
            }
            if(arr[y1][y2][y3][y4][y5][y6][y7]==null){
                arr[y1][y2][y3][y4][y5][y6][y7]=new long[8][];
            }
            if(arr[y1][y2][y3][y4][y5][y6][y7][y8]==null){
                arr[y1][y2][y3][y4][y5][y6][y7][y8]=new long[8];
            }
            arr[y1][y2][y3][y4][y5][y6][y7][y8][y9]=arr[y1][y2][y3][y4][y5][y6][y7][y8][y9] | (1L<<(i & 0x3F));
        }
    }

}
