<!-- 审批配置 -->
<!--
审批的 JSON 格式:
{
    type: 'ORDER',
    steps: [
        {
            step: 1,
            auditors: [
                userId: 123,
                username: 'Alice'
            ]
        },
    ]
}
-->
<template>
    <div class="audit-config">
        <div class="toolbar">
            <!-- 审批类型 -->
            <ButtonGroup class="audit-types">
                <Button v-for="type in window.AUDIT_TYPES" :key="type.value" :type="buttonType(type)" @click="switchType(type)">{{ type.name }}</Button>
            </ButtonGroup>

            <!-- 保存按钮 -->
            <Button type="primary" :disabled="!hasPermissionOfSuperAdmin()" @click="save()">保存</Button>
        </div>


        <!-- 审批流程 -->
        <div class="box">
            <div class="title">审批流程</div>
            <div class="content audit-steps">
                <div v-for="(step, index) in auditConfig.steps" :key="'step-' + step.uid" class="audit-step">
                    <div class="toolbar">
                        <Poptip trigger="hover" placement="right" width="400">
                            <Button size="small" type="dashed">审批模板</Button>
                            <Input v-model="step.commentTemplate" type="textarea" slot="content" :rows="8" placeholder="请填写审批意见的模板"/>
                        </Poptip>
                        <Checkbox v-model="step.attachment">上传附件</Checkbox>
                        <div class="stretch"></div>

                        <!-- 删除阶段按钮，只有阶段数大于 1 时才显示 -->
                        <Icon v-if="auditConfig.steps.length > 1" type="md-close-circle" class="clickable" size="18" @click="removeStep(index)"/>
                    </div>

                    <!-- 审批员 -->
                    <div class="audit-step-auditors" :id="step.uid" :data-id="step.uid">
                        <div v-for="auditor in step.auditors" :data-id="auditor.userId" :key="auditor.userId" class="auditor">
                            <div class="title">
                                <span>{{ getUserRole(auditor.userId) }}</span>
                                <Icon type="md-close" class="close-icon clickable" @click="removeAuditorFromStep(step, auditor)"/>
                            </div>
                            <div class="name">{{ auditor.nickname }}</div>
                        </div>
                    </div>

                    <!-- 增加下一阶段按钮 -->
                    <div class="add-step">
                        <Icon type="ios-arrow-round-down" size="35" class="clickable" @click="addStep(index)"/>
                    </div>
                </div>
            </div>
        </div>

        <!-- 未分配用户 -->
        <div class='box'>
            <div class="title">系统用户</div>
            <div class="content rest-auditors" id="rest-auditors">
                <div v-for="auditor in allAuditors" :data-id="auditor.userId" :key="auditor.userId" class="auditor">
                    <div class="title">
                        <span>{{ getUserRole(auditor.userId) }}</span>
                    </div>
                    <div class="name">{{ auditor.nickname }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import UserDao from '@/../public/static-p/js/dao/UserDao';
import AuditDao from '@/../public/static-p/js/dao/AuditDao';
import AuditUtils from '@/../public/static-p/js/utils/AuditUtils';
import Sortable from 'sortablejs';

export default {
    data() {
        return {
            auditType   : { value: '', name: '' }, // 当前的审批类型
            auditConfigs: [], // 所有审批的配置
            auditConfig : {}, // 当前业务的配置: [{ type : 'ORDER', uid: 1, users: [{ nickname: 'Bob', userId: '1' }] }]
            allAuditors : [], // 系统的所有用户
            restAuditors: [], // 当前业务未分配审批的用户
            sortables   : [], // 可拖拽对象
            userRoles   : new Map(), // 用户的角色，key 为 userId, value 为 role
        };
    },
    mounted() {
        // 1. 加载所有用户和审批配置
        // 2. 给每个阶段一个 uid
        // 3. 显示第一个类型的审批配置
        Promise.all([UserDao.findUsers({ pageSize: 10000 }), AuditDao.findAuditConfigs()]).then(([users, configs]) => {
            users.forEach(user => {
                const role = window.ROLES.filter(r => r.value === user.roles[0]).map(r => r.name).join('');
                this.userRoles.set(user.userId, role);
                this.allAuditors.push({ userId: user.userId, username: user.username, nickname: user.nickname });
            });

            // AuditUtils.correctAuditConfigs(configs);

            // [2] 给没给阶段一个 uid
            configs.forEach(config => {
                config.steps.forEach(step => {
                    step.uid = Utils.uid();
                });
            });

            this.auditConfigs = configs;

            // [3] 显示第一个类型的审批配置
            this.switchType(window.AUDIT_TYPES[0]);
        });
    },
    methods: {
        // 切换审批类型
        switchType(type) {
            this.auditType = type;
            this.init(type.value);
        },
        // 按钮的类型: default or primary
        buttonType(type) {
            return this.auditType.value === type.value ? 'primary' : 'default';
        },
        // 初始化，参数为审批的业务类型，例如 ORDER
        init(auditType) {
            // 1. 找到当前类型的审批配置
            // 2. 如果审批配置不存在，则创建
            // 3. 创建 sortable 对象

            // [1] 找到当前类型的审批配置
            this.auditConfig = this.auditConfigs.find(config => config.type === auditType);

            // [2] 如果审批配置不存在或者 steps 为空数组，则创建
            if (!this.auditConfig || this.auditConfig.steps.length === 0) {
                this.auditConfig = { type: this.auditType.value, steps: [this.newStep()] };
                this.auditConfigs.push(this.auditConfig);
            }

            // [3] 创建 sortable 对象
            this.$nextTick(() => {
                new Sortable(document.querySelector('#rest-auditors'), {
                    group: { name: 'fox', pull: 'clone', put: false }, animation: 150, sort: false
                });

                for (let step of this.auditConfig.steps) {
                    new Sortable(document.querySelector(`#${step.uid}`), {
                        group: { name: 'fox', pull: false, put: true }, animation: 150, sort: false, onAdd: this.onAdd
                    });
                }
            });
        },
        // 添加审批阶段
        addStep(index) {
            const step = this.newStep();
            this.auditConfig.steps.splice(index+1, 0, step);

            // 创建 sortable 对象
            this.$nextTick(() => {
                new Sortable(document.querySelector(`#${step.uid}`), {
                    group: { name: 'fox', pull: false, put: true }, animation: 150, sort: false, onAdd: this.onAdd
                });
            });
        },
        // 拖拽放下用户的处理函数
        onAdd(event) {
            // 1. 找到源头的 index 和目标的 index，审批员和阶段
            // 2. 删除 Sortable 创建的 DOM (有的需要删除，有的不用，神奇)
            // 3. 如果在拖放的阶段中没有包含此审批员，则添加

            // [1] 找到源头的 index 和目标的 index，审批员和阶段
            const fromIndex = parseInt(event.oldIndex);
            const toIndex   = parseInt(event.newIndex);
            const auditor   = this.allAuditors[fromIndex];
            const step      = this.findStep(event.to.getAttribute('data-id'));

            // [2] 删除 Sortable 创建的 DOM (有的需要删除，有的不用，神奇)
            event.item.remove();

            // [3] 如果在拖放的阶段中没有包含此审批员，则添加
            const found = step.auditors.find(a => a.userId === auditor.userId);
            if (!found) {
                step.auditors.splice(toIndex, 0, auditor);
            }
        },
        // 查询 stepUid 对应的 step 对象
        findStep(stepUid) {
            return this.auditConfig.steps.find(s => s.uid === stepUid);
        },
        // 从阶段中删除审批员
        removeAuditorFromStep(step, auditor) {
            const index = step.auditors.findIndex(a => a.userId === auditor.userId);

            if (index >= 0) {
                step.auditors.splice(index, 1);
            }
        },
        // 删除阶段
        removeStep(index) {
            // [2] 确认修改
            this.$Modal.confirm({
                title: '确定删除吗?',
                onOk: () => {
                    this.auditConfig.steps.remove(index);
                }
            });
        },
        // 保存审批配置
        save() {
            const configs = AuditUtils.cleanAuditConfigs(this.auditConfigs);
            AuditDao.upsertAuditConfigs(configs).then(() => {
                this.$Message.success('保存成功');
            });
        },
        // 创建审批阶段对象
        newStep() {
            return {
                uid            : Utils.uid(),
                desc           : '',
                auditors       : [],
                attachment     : false,
                commentTemplate: '',
            };
        },
        // 获取用户的角色
        getUserRole(userId) {
            return this.userRoles.get(userId);
        }
    }
};
</script>

<style lang="scss">
.audit-config {
    display: grid;
    grid-template-rows: max-content 1fr;
    grid-template-columns: 1fr 250px;
    grid-gap: 20px 20px;

    > .toolbar {
        display: flex;
        justify-content: space-between;
        grid-column: span 2;

        button {
            min-width: 100px;
        }
    }

    .audit-steps {
        min-height: 500px;

        .toolbar {
            display: flex;
            align-items: center;
            margin-bottom: 5px;

            button {
                margin-right: 15px;
            }

            .clickable {
                display: none;
            }
        }

        .audit-step {
            &:hover .clickable {
                display: block;
            }
        }

        .add-step {
            text-align: center;
            margin: 10px 0;

            .ivu-icon {
                color: #bbb;
            }
        }

        .audit-step-auditors {
            padding: 20px;
            min-height: 90px;
            border: 1px solid rgba(0, 0, 0, .05);
            border-radius: 3px;
            box-shadow: 0 0 10px rgba(0, 0, 0, .1) inset;

            .auditor {
                display: inline-block;
                border: none;
                width: 200px;
                padding: 0;
                margin-right: 15px;
                margin-bottom: 0;
                box-shadow: 0 0 2px rgba(0, 0, 0, 0.3);
                border-radius: 4px;
                overflow: hidden;
                cursor: default;
                background: white;

                .title {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    color: #eee;
                    background: #808695;
                    padding: 5px 10px;

                    .close-icon {
                        display: none;
                        font-size: 18px;
                    }
                }

                .name {
                    padding: 10px;
                }

                &:hover .close-icon {
                    display: inline-block;
                    transition: color .8s;
                }
            }
        }
    }

    .rest-auditors {
        min-height: 500px;

        .auditor {
            border: 1px solid $separatorColor;
            margin-bottom: 10px;
            border-radius: 4px;
            padding: 10px;
            position: relative;
            cursor: default;
            overflow: hidden;
            background: white;

            &:hover {
                background: $backgroundColor;
            }

            .title {
                color: #808695;
                position: absolute;
                top: 0px;
                right: 0px;
                font-size: 12px;
                background: cyan;
                padding: 2px 4px;
                border-bottom-left-radius: 4px;
            }
        }
    }
}
</style>
