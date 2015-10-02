package tempest.services;

import main.resources.MembershipListProtos;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by swapnalekkala on 9/27/15.
 */
public class MembershipListUtil {


    public static MembershipListProtos.MembershipList getNewMembershipList() {
        MembershipListProtos.MembershipList membershipList1 = null;
        try {
            membershipList1 = MembershipListProtos.MembershipList.newBuilder().addMember(MembershipListProtos.MemberId.newBuilder().setIp(InetAddress.getLocalHost().getHostAddress()).setHearbeat(1).setTimestamp(System.currentTimeMillis()).build()).build();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return membershipList1;
    }

    public static synchronized void mergeMembershipList(MembershipListProtos.MembershipList receivedMembershipList,MembershipListProtos.MembershipList membershipList) {
        Map<String,MemberHealth> map1 =getMap(receivedMembershipList);
        Map<String,MemberHealth> map2 =getMap(membershipList);

        for (Map.Entry<String, MemberHealth> entry : map1.entrySet())
        {
            if(map2.containsKey(entry.getKey())) {
                if(map2.get(entry.getKey()).getHeartbeat()< entry.getValue().getHeartbeat())
                map2.get(entry.getKey()).setTimestamp(System.currentTimeMillis());
                map2.get(entry.getKey()).setHeartbeat(entry.getValue().getHeartbeat());
            } else {
                map2.put(entry.getKey(),new MemberHealth(System.currentTimeMillis(),entry.getValue().getHeartbeat()));
            }
        }

        toProtobuf(map2,membershipList);
    }

    private static synchronized void toProtobuf(Map<String, MemberHealth> map, MembershipListProtos.MembershipList membershipList) {

        membershipList.toBuilder().clear();

        for (Map.Entry<String, MemberHealth> entry : map.entrySet())
        {
            membershipList = membershipList.toBuilder().addMember(MembershipListProtos.MemberId.newBuilder().setIp(entry.getKey()).setHearbeat(entry.getValue().getHeartbeat()).setTimestamp(entry.getValue().getTimestamp()).build()).build();
        }
    }

    private static synchronized Map<String,MemberHealth> getMap(MembershipListProtos.MembershipList receivedMembershipList) {
        MembershipListProtos.MembershipList.Builder builder = receivedMembershipList.toBuilder();

        Map<String,MemberHealth> memberHealthMap = new HashMap<String,MemberHealth>();
        int i=0;
        while(builder.getMemberList().size()>i) {
            MemberHealth memberHealth = new MemberHealth(builder.getMember(i).getTimestamp(),builder.getMember(i).getHearbeat());
            memberHealthMap.put(builder.getMember(i).getIp(), memberHealth);
            i++;
        }

        return memberHealthMap;
    }

    public static synchronized void updateMembershipList(MembershipListProtos.MembershipList membershipList) {
        Map<String,MemberHealth> map2 =getMap(membershipList);
        try {
            int heartbeat = map2.get(InetAddress.getLocalHost().getHostAddress()).getHeartbeat();
            map2.get(InetAddress.getLocalHost().getHostAddress()).setHeartbeat(heartbeat + 1);
            map2.get(InetAddress.getLocalHost().getHostAddress()).setTimestamp(System.currentTimeMillis());

            toProtobuf(map2, membershipList);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args) {
//        //MembershipListProtos.MembershipList received = getNewMembershipList();
//        System.out.println(membershipList);
//        try {
//            MembershipListProtos.MembershipList receivedMembershipList = MembershipListProtos.MembershipList.newBuilder().addMember(MembershipListProtos.MemberId.newBuilder().setIp(InetAddress.getLocalHost().getHostAddress()).setHearbeat(10).setTimestamp(System.currentTimeMillis()).build()).build();
//            receivedMembershipList = receivedMembershipList.toBuilder().addMember(MembershipListProtos.MemberId.newBuilder().setIp("10.0.1.28").setHearbeat(10).setTimestamp(System.currentTimeMillis()).build()).build();
//            System.out.println(receivedMembershipList);
//
//            mergeMembershipList(receivedMembershipList);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        System.out.println(membershipList);
//
//        // MembershipListProtos. MembershipList membershipList =
//    }

}
