  <template>
    <a-row class="login" >
      <a-col :span="8" :offset="8" class="login-main">
        <h1 style="text-align: center"><RocketOutlined />12306</h1>
         <a-form
        :model="loginForm"
        name="basic"
        autocomplete="off"

    >
      <a-form-item
          label=""
          name="mobile"
          :rules="[{ required: true, message: '请输入手机号!' }]"
      >
        <a-input v-model:value="loginForm.mobile" placeholder="手机号"/>
      </a-form-item>

      <a-form-item
          label=""
          name="code"
          :rules="[{ required: true, message: '请输入验证码！' }]"
      >
        <a-input v-model:value="loginForm.code" >
          <template #addonAfter>
            <a @click="sendCode">获取验证码</a>
          </template>
        </a-input>

      </a-form-item>


      <a-form-item name="remember" :wrapper-col="{ offset: 0, span: 8 }">
        <a-checkbox v-model:checked="loginForm.remember">Remember me</a-checkbox>
      </a-form-item>

      <a-form-item :wrapper-col="{ offset: 11, span: 8 }">
        <a-button type="primary" @click="login" >登录</a-button>
      </a-form-item>
        </a-form>
      </a-col>
    </a-row>

  </template>
  <script setup>
    import { reactive } from 'vue';
    import axios from "axios";
    import { notification } from 'ant-design-vue';
    import store from "@/store";
    import { useRouter } from "vue-router";

    const router=useRouter();
    const loginForm = reactive({
      mobile:'13000000000',
      code:''
    });

    const sendCode = () => {
    axios.post("/member/member/send-code",//先请求
        {mobile:loginForm.mobile//传递参数 json传递参数方法 展开一个个写
        }).then(response=>{
        let data = response.data;//获取结果的数据
        if (data.success){//判断success
          notification.success({description:'发送验证码成功！'})//成功做什么处理
          loginForm.code="8888"
        }else {
          notification.error({description:data.message})//失败做什么处理
        }

      });
    };
    const login = () => {
      axios.post("/member/member/login",//先请求
          loginForm).//直接传递实体
      then((response)=> {
        let data = response.data;//获取结果的数据
        if (data.success) {
          notification.success({description: '登录成功！'});
          //登录成功，跳到控制台主页
          router.push("/welcome")
          store.commit("setMember",data.content);//调用store里的store方法使用这种格式，调用store中的setMember方法和参数data.content
        } else {
          notification.error({description: data.message});
        }
      })
    }

  </script>
  <style>
.login-main h1{
  font-size: 25px;
  font-weight: bold;
}
.login-main{
  margin-top: 100px;
  padding: 30px 30px 20px;
  border: 2px solid grey;
  border-radius: 10px;
  background-color: #fcfcfc;
}
  </style>

