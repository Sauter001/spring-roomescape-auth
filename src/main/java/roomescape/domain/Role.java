package roomescape.domain;

public enum Role {
    USER, ADMIN, MANAGER;

    public boolean hasAdminAccess() {
        return this == ADMIN || this == MANAGER;
    }
}
