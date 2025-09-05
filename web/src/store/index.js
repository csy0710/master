import { createStore } from 'vuex'

export default createStore({//这是存储全局变量的容器
  state: {//类似java实体类，声明一个属性。
    member:{}
  },

  getters: {//数值或格式的转换
  },

  mutations: {//相当于java中的set方法，对属性的修改都写在这里。

    setMember(state,_member){//_member是外部传进来的参数，而member是state中的参数
      state.member=_member;
    }
  },

  actions: {//设置一个异步任务
  },


  modules: {//模块
    a:{

    }
  }
})
