<!-- 存储函数可视化执行 -->
<template>
    <div class="stored-function">
        <!-- 函数列表 -->
        <List header="存储函数" border>
            <ListItem v-for="name in functionNames" :key="name" @click.native="selectFunction(name)">{{ name }}</ListItem>
        </List>

        <!-- 执行函数部分 -->
        <div class="function-execute">
            <span>签名:</span>
            <div>{{ func.signature }}</div>

            <!-- 函数的参数，用户输入 -->
            <span>参数:</span>
            <div class="function-params">
                <template v-for="(value, idx) in funcArguments">
                    <Input v-model="funcArguments[idx]" :key="idx" :placeholder="`${func.inArgs[idx].name} - ${func.inArgs[idx].dataTypeName}`"/>
                </template>

                <!-- supported 为 false 则不支持 -->
                <Button type="primary" :disabled="!func.supported" :loading="executing" @click="executeFunction">执行</Button>
            </div>

            <!-- 显示函数的执行结果 -->
            <span>结果:</span>
            <div>{{ funcResult }}</div>
        </div>
    </div>
</template>

<script>
import StoredFunctionDao from '@/../public/static-p/js/dao/StoredFunctionDao';

export default {
    data() {
        return {
            functionNames: [],         // 选择的 schema 下的函数名数组。
            func         : {},         // 当前选中的函数。
            funcArguments: [],         // 函数的参数值。
            funcResult   : {},         // 函数执行结果。
            executing    : false,      // 执行中。
            catalog      : 'postgres', // 数据库。
            schema       : 'biao',     // 模式。
        };
    },
    mounted() {
        // 查询出所有的存储函数名。
        StoredFunctionDao.listFunctionNames(this.catalog, this.schema).then(functionNames => {
            this.functionNames = functionNames;
        });
    },
    methods: {
        // 选择函数。
        selectFunction(functionName) {
            this.funcResult = {}; // 清楚执行结果。

            StoredFunctionDao.findFunction(this.catalog, this.schema, functionName).then(func => {
                this.func = func;
                this.funcArguments = new Array(func.inArgs.length);
                console.log(func);
            });
        },
        // 执行函数。
        executeFunction() {
            console.log(this.funcArguments);

            // 要执行的函数信息。
            const funcForm = {
                catalog          : this.func.catalog,
                schema           : this.func.schema,
                functionName     : this.func.name,
                functionArguments: this.funcArguments,
            };

            this.executing = true;
            StoredFunctionDao.executeFunction(funcForm).then(funcResult => {
                this.executing = true;
                this.funcResult = funcResult;
                this.executing = false;
            }).catch(err => {
                console.error(err);
                this.executing = false;
            });
        }
    }
};
</script>

<style lang="scss">
.stored-function {
    display: grid;
    grid-template-columns: 350px 1fr;
    grid-column-gap: 10px;

    .ivu-list-item {
        cursor: pointer;

        &:hover {
            color: #2d8cf0;
        }
    }

    .function-execute {
        display: grid;
        grid-template-columns: max-content 1fr;
        grid-template-rows: max-content max-content 1fr;
        grid-gap: 10px 10px;

        .ivu-input-wrapper {
            width: 200px;
            display: block;
            margin-bottom: 5px;
        }
    }
}
</style>
