import java.util.*;
import java.util.Collections;

class Solution {
    public int solution(int[] people, int limit) {
        Arrays.sort(people);

        for (int i = 0; i < people.length / 2; i++) {
            int temp = people[i];
            people[i] = people[people.length - 1 - i];
            people[people.length - 1 - i] = temp;
        }

        int cnt = 0;
        boolean[] rescued = new boolean[people.length];
        for(int i = 0; i < people.length; i++) {
            if(rescued[i]) continue;
            int man = people[i];
            int free = limit - man;

            rescued[i] = true;
            if(free == 0) {
                cnt++;
                continue;
            }

            //System.out.println("cnt: " + cnt);
            for(int t=i+1; t < people.length; t++) {

                if(rescued[t]) continue;
                int pair = people[t];

                //System.out.println("free: " + free + " free - pair: " + (free - pair));
                if(free - pair >= 0) {
                    rescued[t] = true;
                    break;
                }
            }

            cnt++;
        }

        return cnt;
    }

    //	[80,70, 50, 50]
}