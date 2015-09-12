package tempest.interfaces;

public interface CommandResponse<TSelf extends CommandResponse> {
    TSelf add(TSelf response);
}
