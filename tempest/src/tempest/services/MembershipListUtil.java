package tempest.services;

import tempest.protos.Membership;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class MembershipListUtil {

    public static Membership.MembershipList getNewMembershipList() {
        Membership.MembershipList newMembershipList = null;
        try {
            newMembershipList = Membership.MembershipList.newBuilder().addMember(Membership.Member.newBuilder().setHost(InetAddress.getLocalHost().getHostName()).setHearbeat(1).setTimestamp(System.currentTimeMillis()).setNodeStatus(NodeStatus.ACTIVE.name()).build()).build();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return newMembershipList;
    }

    public static synchronized Membership.MembershipList mergeMembershipList(Membership.MembershipList receivedMembershipList, Membership.MembershipList membershipList) {
        Map<String, MemberHealth> receivedMap = getMap(receivedMembershipList);
        Map<String, MemberHealth> currentMap = getMap(membershipList);

        for (Map.Entry<String, MemberHealth> entry : receivedMap.entrySet()) {
            if (currentMap.containsKey(entry.getKey())) {
                currentMap.get(entry.getKey()).setNodeStatus(entry.getValue().getNodeStatus());
                if (currentMap.get(entry.getKey()).getHeartbeat() < entry.getValue().getHeartbeat()) {
                    currentMap.get(entry.getKey()).setTimestamp(System.currentTimeMillis());
                    currentMap.get(entry.getKey()).setHeartbeat(entry.getValue().getHeartbeat());
                }
            } else {
                if (!entry.getValue().getNodeStatus().equals(NodeStatus.LEAVE)) {
                    currentMap.put(entry.getKey(), new MemberHealth(System.currentTimeMillis(), entry.getValue().getHeartbeat(), entry.getValue().getNodeStatus()));
                }
            }
        }

        return toProtobuf(currentMap);
    }

    public static synchronized Membership.MembershipList addMemberToMembershipList(Membership.Member member, Membership.MembershipList membershipList) {
        Membership.MembershipList.Builder membershipListBuilder = membershipList.toBuilder();

        membershipListBuilder.addMember(member);

        return membershipListBuilder.build();
    }

    private static synchronized Membership.MembershipList toProtobuf(Map<String, MemberHealth> map) {

        Membership.MembershipList.Builder membershipListBuilder = Membership.MembershipList.newBuilder();

        for (Map.Entry<String, MemberHealth> entry : map.entrySet())
        {
           membershipListBuilder.addMember(Membership.Member.newBuilder().setHost(entry.getKey()).setHearbeat(entry.getValue().getHeartbeat()).setTimestamp(entry.getValue().getTimestamp()).build());
        }
        return membershipListBuilder.build();
    }

    private static synchronized Map<String, MemberHealth> getMap(Membership.MembershipList receivedMembershipList) {
        Membership.MembershipList.Builder builder = receivedMembershipList.toBuilder();

        Map<String, MemberHealth> memberHealthMap = new HashMap<String, MemberHealth>();
        int i = 0;
        while (builder.getMemberList().size() > i) {
            MemberHealth memberHealth = new MemberHealth(builder.getMember(i).getTimestamp(), builder.getMember(i).getHearbeat(), NodeStatus.valueOf(builder.getMember(i).getNodeStatus()));
            memberHealthMap.put(builder.getMember(i).getHost(), memberHealth);
            i++;
        }

        return memberHealthMap;
    }

    public static synchronized Membership.MembershipList updateMembershipList(Membership.MembershipList membershipList) {
        Map<String, MemberHealth> map2 = getMap(membershipList);
        try {
            int heartbeat = map2.get(InetAddress.getLocalHost().getHostName()).getHeartbeat();
            map2.get(InetAddress.getLocalHost().getHostName()).setHeartbeat(heartbeat + 1);
            map2.get(InetAddress.getLocalHost().getHostName()).setTimestamp(System.currentTimeMillis());

            return toProtobuf(map2);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return membershipList;
    }
}
