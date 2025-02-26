/**
 * 对 axios 根据 restful 风格进行的封装，不管是什么类型的请求，都统一了参数的格式 { url, pathVariables, data, json, config }，
 * 只有 url 是必要的参数，其他几个参数是可选的，根据需求传入，支持下面 4 种请求:
 * 获取资源: Rest.get()    -> GET  请求
 * 创建资源: Rest.create() -> POST 请求
 * 更新资源: Rest.update() -> PUT  请求
 * 部分更新: Rest.patch()  -> PATCH  请求
 * 删除资源: Rest.remove() -> DELETE 请求
 *
 * 这 4 个函数都返回 Promise 对象，then 的参数为请求成功的响应，catch 的参数为失败的响应, 调用示例
 * [1] 普通 GET 请求
 * Rest.get({ url: '/api/rest', data: { pageNumber: 3 } }).then(result => {
 *     console.log(result);
 * });
 *
 * [2] 替换 url 中的变量: {bookId} 会被替换为 pathVariables.bookId 的值 23，得到请求的 url '/rest/books/23'
 * Rest.update({ url: '/rest/books/{bookId}', pathVariables: { bookId: 23 }, data: { name: 'C&S' } }).then(result => {
 *     console.log(result);
 * });
 *
 * [3] 设置 json 为 true 使用 request body 传输复杂的 data 对象 (有多级属性)
 * json 默认为 false，使用 application/x-www-form-urlencoded 的方式，即普通表单的方式
 * Rest.get({ url: '/api/rest', data: { user: { username: 'Bob', password: '123456' }, company: 'Appo' }, json: true }).then(result => {
 *     console.log(result);
 * });
 *
 * [4] axios 不支持同步请求，但可以在同一个函数里使用 async await 进行同步操作:
 * async function syncFunc() {
 *     const r1 = await Rest.get({ url: '/api/rest' }); // r1 为 then 或者 catch 的参数
 *     const r2 = await Rest.create({ url: '/api/rest', data: { name: 'Goqu' } });
 *
 *     console.log(r1, r2);
 * }
 * 注: jQuery 的 Ajax 支持同步请求，但是新版本中也不推荐使用了，浏览器中会有警告
 *
 * 提示:
 *     错误处理: 绝大多数时候不需要在 catch 中进行错误处理，已经默认提供了 401，403，404，服务器抛异常时的 500，服务不可达的 502 等错误处理: 弹窗提示和控制台打印错误信息。
 *     额外参数: 可以在 config 参数中传入 axios 的其他配置，但是这几个参数不受 config 中的配置影响: url, data, params, method, responseType, Content-Type, X-Requested-With
 *     合并参数: data 综合了 axios 的 params 和 data 参数
 */
class Rest {
    /**
     * 使用 Ajax 的方式异步执行 REST 的 GET 请求 (服务器响应的数据根据 REST 的规范，必须是 Json 对象，否则浏览器端会解析出错)。
     * 以下几个 REST 的函数 Rest.create(), Rest.update(), Rest.remove() 等和 Rest.get() 的参数都是一样的，就不再重复注释了。
     *
     * @param {Json} options 有以下几个选项:
     *      {String}  url           请求的 URL (必填)
     *      {Json}    pathVariables URL 路径中的变量，例如 /rest/users/{id}，其中 {id} 替换为 pathVariables.id 的值 [可选]
     *      {Json}    data          请求的参数  [可选]
     *      {Boolean} json          是否使用 application/json 的方式进行请求，默认为 false 使用 application/x-www-form-urlencoded [可选]
     *      {Json}    config        Axios 的 config [可选] (参考 https://www.kancloud.cn/yunye/axios/234845)，需要注意的是下面几个参数不受 config 影响:
     *                              url, data, params, method, responseType, Content-Type, X-Requested-With
     * @return 返回 Promise 对象, then 的参数为请求成功的响应, catch 的参数为失败的响应
     */
    static get(options) {
        options.method = 'GET';
        return Rest.executeRequest(options);
    }

    static create(options) {
        options.method = 'POST';
        return Rest.executeRequest(options);
    }

    static update(options) {
        options.method = 'PUT';
        return Rest.executeRequest(options);
    }

    static patch(options) {
        options.method = 'PATCH';
        return Rest.executeRequest(options);
    }

    static remove(options) {
        options.method = 'DELETE';
        return Rest.executeRequest(options);
    }

    static upload(url, formData) {
        return new Promise((resolve, reject) => {
            axios.post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then(response => {
                resolve(response.data);
            }).catch(response => {
                console.error(error);
                const error = response.response;
                reject(error);
            });
        });
    }

    /**
     * 执行请求
     */
    static executeRequest({ url, pathVariables, data, json, method, config }) {
        return new Promise((resolve, reject) => {
            if (!url) {
                console.error('URL undefined');
                reject();
                return;
            }

            // json 为 false，构建 POST, PUT, DELETE 请求的参数
            // 注: 此处结合 Spring MVC 的 HiddenHttpMethodFilter 拦截器，使用 POST 执行 PUT, PATCH, DELETE 请求，
            //     PUT 时需要额外参数 _method=PUT，DELETE 时需要 _method=DELETE
            //     服务器端是其他框架的话，根据具体情况进行修改
            if (!json && method !== 'GET') {
                data = Rest.formDataString({ ...data, _method: method });
                method = 'POST';
            }

            // content type 为 applicatoin/json 时 data 为 json 对象，使用 request body 传输
            // content type 为 application/x-www-form-urlencoded 时 data 为 key=value 字符串 (FormData)
            let options = Object.assign({ headers: {} }, config, {
                url: Rest.formatUrl(url, pathVariables), // 替换 URL 上的占位变量
                data,
                params: (method === 'GET') ? data: {}, // GET 请求时 URL 后面的 query parameters
                method,
                responseType: 'json',
            });

            // 服务器抛异常时，有时 Windows 的 Tomcat 环境下竟然取不到 header X-Requested-With, Mac 下没问题，
            // 正常请求时都是好的，手动添加 X-Requested-With 为 XMLHttpRequest 后所有环境下正常和异常时都能取到了
            options.headers['X-Requested-With'] = 'XMLHttpRequest';
            options.headers['Content-Type'] = json ? 'application/json;charset=UTF-8' : 'application/x-www-form-urlencoded';

            // 执行请求
            axios(options).then(response => {
                resolve(response.data);
            }).catch(response => {
                const error = response.response;
                Rest.handleError(error);
                reject(error);
            });
        });
    }

    /**
     * 错误处理
     */
    static handleError(error) {
        const status = error.status;

        if (401 === status) {
            alert('401: Token 无效');
        } else if (403 === status) {
            alert('403: 权限不够');
        } else if (404 === status) {
            alert('404: URL 不存在');
        } else if (500 === status) {
            // 发生 500 错误时服务器抛出异常，在控制台打印出异常信息
            console.error(error);

            if (error.data && error.data.message) {
                alert(`500: 发生异常，${error.data.message}\n\n详细错误信息请查看控制台输出 (Chrome 按下快捷键 F12)`);
            }
        } else if (502 === status) {
            // 发生 502 错误时，Tomcat Web 服务器不可访问，一般有 2 个原因
            // 1. Nginx 配置出错
            // 2. Tomcat 的 Web 服务没启动或者不接收请求
            alert('502: 服务不可访问');
        } else if (504 === status) {
            alert('504: Gateway Timeout\n\n' + error.statusText);
        }
    }

    /**
     * 替换 URL 路径中的变量，例如 /rest/users/{id}，其中 {id} 替换为 pathVariables.id 的值
     *
     * @param {String} url           URL 例如 /rest/users/{id}，如果没有占位的变量，则原样返回
     * @param {Json}   pathVariables Json 对象
     * @return {String} 返回替换变量后的 URL
     */
    static formatUrl(url, pathVariables) {
        if (!pathVariables) {
            return url;
        }

        // 查找 {{、}}、或者 {name}，然后进行替换
        // m 是正则中捕获的组 $0，即匹配的整个子串
        // n 是正则中捕获的组 $1，即 () 中的内容
        // function($0, $1, $2, ...)
        return url.replace(/\{\{|\}\}|\{(\w+)\}/g, function(m, n) {
            if (m === '{{') { return '{'; }
            if (m === '}}') { return '}'; }

            return pathVariables[n];
        });
    }

    /**
     * 序列化 data 为 key value 的字符串 key1=value1&key2=value2
     *
     * @param {Json} data Json 对象
     * @return 返回 key value 的字符串
     */
    static formDataString(data) {
        return [...Object.keys(data)]
            .map(key => encodeURIComponent(key) + '=' + encodeURIComponent(data[key]))
            .join('&');
    }
}

window.Rest = Rest;
