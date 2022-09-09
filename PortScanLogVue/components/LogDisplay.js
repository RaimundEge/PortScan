app.component('log-display', {
  template:
    /*html*/
    `<div class="log-display">        
       <div class="log-records">
         <div v-for="group in Object.keys(groups)">
           <div class="log-group">           
             <div v-on:click="toggleGroup(group)" class="log-header">
               <div class="group-line"><div v-if="!showGroup[group]"> + </div>
               <div v-else> - </div>
               &nbsp;{{ group }}</div><div>{{ groups[group].length }} connections</div>
             </div>
             <div v-if="showGroup[group]" class="group-detail">              
               <div v-for="log in groups[group]">
                 <div class="log-line space-between">
                   <div v-bind:class="{ selected: current===log['id'] }" v-on:click="getDetail(log['id'])">{{ formatLog(log) }}</div>
                 </div>
               </div>
             </div>
           </div>        
         </div>
       </div>
       <div class="details">
         <div class="detailHeader">
          Duration: <input v-model.number="month" type="number" class="month" /> months &nbsp; &nbsp;
          {{ count }} records &nbsp; &nbsp;
          <button @click="getLogRecords()">Update</button>         
         </div>
         <hr>         
         <div v-if="detail!=null">
           <h3>{{ detail.group }}</h3>
           <div class="detail">
             {{ detail.record }}
           </div>
         </div>
       </div>      
    </div>`,
  data() {
    return {
      data: null,
      month: 3,
      oldMonth: 3,
      detail: null,
      current: null,
      groups: {},
      showGroup: {}
    }
  },
  methods: {
    async getLogRecords() {
      NProgress.start()
      var resp = await axios.get('../logrecords/records?month=' + this.month)
      // console.log(resp.data);
      this.data = resp.data;
      this.groups = [];
      // process groups
      for (var log of this.data) {
        if (this.groups[log.group] == null) {
          this.groups[log.group] = [log]
        } else {
          this.groups[log.group].push(log)
        }
        this.showGroup[log.group] = false
      }
      NProgress.done()
      // console.log(Object.keys(this.groups).length + " number of groups")
    },
    toggleGroup(group) {
      for (var sg in this.showGroup) {
        if (sg == group) {
          this.showGroup[sg] = !this.showGroup[sg]
        } else {
          this.showGroup[sg] = false
        }
      }
      this.current = null
      this.detail = null
    },
    formatLog(log) {
      var result = log['id'] + ': '
      result += log['timestamp'] + ', '
      result += log['group'] + ' >'
      result += log['IP'] + ':' + log['port'] + '<, '
      result += log['type']
      return result
    },
    async getDetail(id) {
      var response = await axios.get('../logrecords/record?id=' + id)
      this.detail = response.data
      this.current = id
      // console.log(id + ':' + this.detail)
    }
  },
  computed: {
    count() {
      if (this.data == null || this.month != this.oldMonth) {
        this.oldMonth = this.month;
        this.getLogRecords()
      } else {
        return this.data.length
      }
    },
    list() {
      if (this.data == null) {
        return []
      } else {
        return this.data
      }
    }
  }
})