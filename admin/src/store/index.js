import { createStore } from 'vuex'
const MEMBER = "MEMBER";//往session中放东西需要一个key，所以定义一个常量。
export default createStore({//这是存储全局变量的容器
  state: {//类似java实体类，声明一个属性。
    //第一次打开页面在缓存中看有没有key值
    member:window.SessionStorage.get(MEMBER) || {}  //在session缓存中获取一个key，它有可能是一个空对象，所以用.get(MEMBER) || {}格式，避免空指针异常
  },

  getters: {//数值或格式的转换
  },

  mutations: {//相当于java中的set方法，对属性的修改都写在这里。

    setMember(state,_member){//_member是外部传进来的参数，而member是state中的参数
      state.member=_member;//当memeber属性有变动时，在缓存中更新一下。
      window.SessionStorage.set(MEMBER,_member);
    }
  },

  actions: {//设置一个异步任务
  },


  modules: {//模块
    a:{

    }
  }
})
