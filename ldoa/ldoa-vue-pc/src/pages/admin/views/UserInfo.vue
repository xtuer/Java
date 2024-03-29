<template>
    <div class="user-info-page">
        <!-- 头像 -->
        <div @click="$refs.fileUpload.show()" class="avatar" :style="avatarStyle()"></div>

        <!-- 基本信息 -->
        <div class="base-info">
            <div class="text-color-gray">名字:</div><div>{{ me.nickname }}</div>
            <div class="text-color-gray">账号:</div><div>{{ me.username }}</div>
            <div class="text-color-gray">权限:</div><div>{{ me.roles.join('') | roleName }}</div>

            <!-- 手机号码 -->
            <div class="text-color-gray">手机:</div>
            <div v-if="!modeChangeMobile">
                {{ me.mobile || '无' }}
                <Icon type="md-create" class="clickable" @click="showChangeMobile"/>
            </div>
            <!-- 手机号码编辑 -->
            <div v-else style="width: 200px">
                <Input v-model="mobile" placeholder="请输入手机号码"/>

                <div class="margin-top-10">
                    <Button class="margin-right-20" @click="modeChangeMobile = false">取消</Button>
                    <Button type="primary" :loading="saving" @click="changeMobile">确定</Button>
                </div>
            </div>

            <!-- 密码 -->
            <div class="text-color-gray">密码:</div>
            <div v-if="!modeChangePassword">
                ********
                <Icon type="md-create" class="clickable" @click="showChangePassword"/>
            </div>
            <!-- 密码编辑 -->
            <div v-else style="width: 200px">
                <div>旧密码: <Input v-model="password.oldPassword" type="password" placeholder="请输入旧密码"/></div>
                <div class="margin-top-10">新密码: <Input v-model="password.newPassword" type="password" placeholder="请输入新密码"/></div>
                <div class="margin-top-10">重复新密码: <Input v-model="password.renewPassword" type="password" placeholder="请重复输入新密码"/></div>

                <div class="margin-top-10">
                    <Button class="margin-right-20" @click="modeChangePassword = false">取消</Button>
                    <Button type="primary" :loading="saving" @click="changePassword">确定</Button>
                </div>
            </div>
        </div>

        <!-- 头像上传组件 -->
        <FileUpload ref="fileUpload" image button-hide @on-success="avatarUploaded"/>
    </div>
</template>

<script>
import UserDao from '@/../public/static-p/js/dao/UserDao';
import FileUpload from '@/components/FileUpload.vue';

export default {
    components: { FileUpload },
    data() {
        return {
            modeChangeMobile: false,
            modeChangePassword: false,
            password: {
                oldPassword  : '',
                newPassword  : '',
                renewPassword: '',
            },
            mobile: '', // 手机号码
            saving: false,
        };
    },
    computed: {
        me() {
            return this.$store.state.user;
        },
    },
    mounted() {},
    methods: {
        // 显示修改密码表单
        showChangePassword() {
            this.modeChangeMobile = false;
            this.modeChangePassword = true;
            this.password = { oldPassword  : '', newPassword  : '', renewPassword: '' };
        },
        // 修改密码
        changePassword() {
            // 1. 密码校验
            // 2. 保存新密码到服务器
            // 3. 隐藏密码修改表单

            // [1] 密码校验
            if (!this.password.oldPassword) {
                this.$Message.error('请输入旧密码');
                return;
            }

            if (!this.password.newPassword) {
                this.$Message.error('请输入新密码');
                return;
            }

            if (this.password.newPassword !== this.password.renewPassword) {
                this.$Message.error('新密码不匹配');
                return;
            }

            // [2] 保存新密码到服务器
            this.saving = true;
            UserDao.patchUser({
                userId: this.me.userId,
                oldPassword: this.password.oldPassword,
                newPassword: this.password.newPassword,
                renewPassword: this.password.renewPassword,
            }).then(() => {
                this.saving = false;
                this.modeChangePassword = false; // [3] 隐藏密码修改表单
            }).catch(() => {
                this.saving = false;
            });
        },
        // 显示修改手机号码表单
        showChangeMobile() {
            this.modeChangeMobile = true;
            this.modeChangePassword = false;
        },
        // 修改手机号码
        changeMobile() {
            this.mobile = this.mobile.trim();

            this.saving = true;
            UserDao.patchUser({
                userId: this.me.userId,
                mobile: this.mobile,
            }).then(() => {
                this.me.mobile = this.mobile;
                this.saving = false;
                this.modeChangeMobile = false; // [3] 隐藏密码修改表单
            }).catch(() => {
                this.saving = false;
            });
        },
        // 头像的样式
        avatarStyle() {
            const url = this.me.avatar ? this.me.avatar : '/static-p/img/avatar.jpg';
            return {
                backgroundImage: `url(${url})`
            };
        },
        // 上传头像成功
        avatarUploaded(file) {
            UserDao.patchUser({ userId: this.me.userId, avatar: file.url }).then(avatar => {
                this.me.avatar = avatar;
            });
        }
    }
};
</script>

<style lang="scss">
.user-info-page {
    display: flex;
    flex-direction: column;
    margin-left: 30px;
    margin-top: 30px;

    .avatar {
        // background-image   : url(apple.png);
        background-repeat  : no-repeat;
        background-position: center;
        background-size: cover;

        width : 90px;
        height: 90px;
        margin-left  : 20px;
        border-radius: 6px;
        box-shadow   : 0 0 2px #ccc; /* box-shadow: 0 0 1px #bbb; */

        &:hover {
            box-shadow: 0 0 4px #ccc;
            transition: box-shadow .4s;
            cursor: pointer;
        }
    }

    .base-info {
        display: grid;
        grid-template-columns: max-content 1fr;
        grid-gap: 20px 10px;
        margin-top: 20px;
    }
}
</style>
