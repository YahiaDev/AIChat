
<template>
  <div class="q-pa-md row justify-center">
    <div style="width: 100%; max-width: 700px">
      <q-chat-message label='Monday, 1 January 2024' />


      <q-chat-message v-for="message in messages" :key="message.text" :name="message.from" :text="[message.text]"
        :avatar="message.avatar" :sent="message.from == 'OpenAI' ? true : false" />
      <q-spinner-dots size="2rem" :class="{ 'hidden': showSpinner }" center />


      <q-input rounded standout v-model="newMessage" label="Your message" @keyup.enter="handleEnter"
        :disable="messageDisabled">


      </q-input>
    </div>

  </div>
</template>


<script lang="ts">
import { defineComponent } from 'vue'
import { Notify } from 'quasar'
import { Client } from '@stomp/stompjs';
//import { api } from 'boot/axios'
import { useQuasar } from 'quasar'


export default defineComponent({


  data() {
    return {
      chatSessionId: Math.floor(Math.random() * 10000),
      audioOpenAi: new Audio('src/assets/open-ia-message.mp3'),
      audioGoogleGemini: new Audio('src/assets/gemini-message.mp3'),
      $q: useQuasar(),
      showSpinner: true,
      client: null,
      messageDisabled: false,
      newMessage: '',
      messages: []
    }
  },
  mounted() {
    this.initStomp();

  },


  created() {
    // this.connectToWebSocket();
  },
  setup() {


    return {
    }
  },

  methods: {
    handleEnter() {
      this.messageDisabled = true;
      this.showSpinner = false;

      var data = {
        'message': this.newMessage,
        'chatSessionId': this.chatSessionId
      };


      this.client.publish({ destination: '/app/startChat', body: JSON.stringify(data) });


    },
    playSoundOpenIaMessage() {
      this.audioOpenAi.play();
    },
    playSoundOpenGoogleGeminiMessage() {
      this.audioGoogleGemini.play();
    },
    initStomp() {
      this.client = new Client();
      this.client.configure({
        brokerURL: 'ws://localhost:8081/ws',
        onConnect: () => {
          console.log('onConnect');
          var openAISocketUrl = '/all/openAIMessages-' + this.chatSessionId;
          this.client.subscribe(openAISocketUrl, message => {
            console.log('message received from open ia', message.body);
            this.showOpenGoogleGeminiSpinner = true;
            this.showOpenIaSpinner = false;
            this.playSoundOpenIaMessage();
            this.messages.push({
              text: message.body,
              from: 'OpenAI',
              avatar: 'src/assets/openai-avatar.png'
            });

          });

          var googleGeminiSocketUrl = '/all/googleGeminiMessages-' + this.chatSessionId;
          this.client.subscribe(googleGeminiSocketUrl, message => {
            console.log('message received from google gemini', message.body);
            this.showOpenGoogleGeminiSpinner = false;
            this.showOpenIaSpinner = true;
            this.playSoundOpenGoogleGeminiMessage();

            this.messages.push({
              text: message.body,
              from: 'Google Gemini',
              avatar: 'src/assets/gemini.png'
            });
            //  this.setState({ serverTime: message.body });
          });


          var stopChatUrl = '/all/stop-' + this.chatSessionId;
          this.client.subscribe(stopChatUrl, message => {
            console.log('end chat', message.body);
            this.showSpinner = true;
            Notify.create({
              type: 'positive',
              message: 'The conversation is ended !'
            })



          });

        },
        // Helps during debugging, remove in production
        debug: (str) => {
          console.log(new Date(), str);
        }
      });
      this.client.activate();

    },

  }

});
</script>
