package com.ytjojo.domin.vo;

import com.ytjojo.http.ResponseWrapper;


/**
 * Created by Administrator on 2016/10/27 0027.
 */
public class LoginResponse extends ResponseWrapper<LoginResponse.UserRoles> {


    public LoginResponse(int code, String msg, LoginResponse.UserRoles data) {
        super(code, msg, data);
    }
    public LoginResponse (){
    }
    public Tokens properties;

    public static class Tokens {
        private String accessToken;

        public void setAccessToken(String accessToken)
        {
            this.accessToken = accessToken;
            System.out.println("accessToken = " + accessToken);
        }

        public String getAccessToken()
        {
            return accessToken;
        }

    }
    public static class UserRoles implements java.io.Serializable {
        private static final long serialVersionUID = 6714830399642228876L;

        //(alias="用户角色编号")
        private Integer id;//用于环信登录注册作为username的一部分，完整username 为 doctort_+id;

        //(alias="用户编号")
        private String userId;

        //(alias="角色类型")
        private String roleId;

        //(alias="")
        private String tenantId;

        //(alias="机构层级编码")
        private String manageUnit;

        //(alias="最后登陆时间")
        private String lastLoginTime;

        //(alias="最后登陆IP地址")
        private String lastIPAddress;

        //(alias="最后用户代理")
        private String lastUserAgent;

        //add by zhengji begin
        private String roleName;
        private String tenantName;
        private String userName;
        private String displayName;
        private String manageUnitName;
        private String userAvatar;
        //(strategy = IDENTITY)
        //(name = "id", unique = true, nullable = false)
        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        //(name = "userId", nullable = false, length = 20)
        public String getUserId() {
            return this.userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        //(name = "roleId", nullable = false, length = 50)
        public String getRoleId() {
            return this.roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        //(name = "tenantId", length = 50)
        public String getTenantId() {
            return this.tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        //(name = "manageUnit", nullable = false, length = 50)
        public String getManageUnit() {
            return this.manageUnit;
        }

        public void setManageUnit(String manageUnit) {
            this.manageUnit = manageUnit;
        }
        public String getDisplayName() {
            return this.displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        public String getUserName() {
            return this.userName;
        }

        public void setuserName(String userName) {
            this.userName = userName;
        }
        public String getTenantName() {
            return this.tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }
        public String getRoleName() {
            return this.roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        //(name = "lastLoginTime", length = 19)
        public String getLastLoginTime() {
            return this.lastLoginTime;
        }

        public void setLastLoginTime(String lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        //(name = "lastIPAddress", length = 50)
        public String getLastIPAddress() {
            return this.lastIPAddress;
        }

        public void setLastIPAddress(String lastIpaddress) {
            this.lastIPAddress = lastIpaddress;
        }

        //(name = "lastUserAgent", length = 150)
        public String getLastUserAgent() {
            return this.lastUserAgent;
        }

        public void setLastUserAgent(String lastUserAgent) {
            this.lastUserAgent = lastUserAgent;
        }

        public String getManageUnitName() {
            return this.manageUnitName;
        }

        public void setManageUnitName(String manageUnitName) {
            this.manageUnitName = manageUnitName;
        }

        public String getUserAvatar() {
            return this.userAvatar;
        }

        public void setUserAvatar(String userAvatar) {
            this.userAvatar = userAvatar;
        }

    }
}
