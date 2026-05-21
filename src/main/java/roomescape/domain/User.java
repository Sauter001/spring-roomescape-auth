package roomescape.domain;

public record User(Long id, String uid, String name, Role role) {
   public boolean hasRole(Role role) {
       return this.role == role;
   }

    public boolean isAdmin() {
       return hasRole(Role.ADMIN);
    }
}
