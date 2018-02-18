<?php 

if (!isset($_REQUEST)) { 
  return; 
} 

//Строка для подтверждения адреса сервера из настроек Callback API 
$confirmation_token = 'ctoken'; 

//Ключ доступа сообщества 
$token = 'token'; 

$data = json_decode(file_get_contents('php://input')); 

//Хост, логин, пароль, бд
$mysqli = new mysqli("localhost", "root", "", "testbd");

//Таблица авторизаций
$database = "Auth";

//Колонка с ником
$nick = "Name";

//Колонка с паролем
$pass = "Password";

//Хеш паролей
//sha256, md5.. etc
$hash = "sha256";

$msg = "";

if ($mysqli->connect_errno) {
    echo('ok');
    exit();
}

switch ($data->type) { 
  case 'confirmation': 
    echo $confirmation_token; 
    break; 

  case 'message_new': 
    
    $user_id = $data->object->user_id;

    $message = $data->object->body;

    $sw = mb_strtolower(explode(" ", $message)[0]);

    switch ($sw) {
       case 'привязать':
          $data = explode(" ", $message);

          if(!isset($data[1]) or !isset($data[2])) {
                  $msg = "Использование: привязать <ник> <пароль>";
                  break;
          }

          $login = $data[1];
          $password = hash($hash, $data[2]);

          $result = $mysqli->query("SELECT * FROM `{$database}` WHERE `{$nick}` = '{$login}'");

          foreach ($result as $key) {
            $rpass = $key[$pass];
            if(!empty($key['VK_ID'])) {
            	$msg = "Учётная запись {$login} уже БЫЛА привязана.";
            	break;
            }
            if($rpass != $password) {
                    $msg = "Пароль введён не верно.";
                  break;
            } else {
                $mysqli->query("UPDATE `{$database}` SET `VK_ID` = '{$user_id}' WHERE `{$nick}` = '{$login}'");
                $msg = "Учётная запись {$login} успешно привязана.";
            }
          } 
         break;

      case 'отвязать':
          $data = explode(" ", $message);

          if(!isset($data[1]) or !isset($data[2])) {
                  $msg = "Использование: отвязать <ник> <пароль>";
                  break;
          }

          $login = $data[1];
          $password = hash($hash, $data[2]);

          $result = $mysqli->query("SELECT * FROM `{$database}` WHERE `{$nick}` = '{$login}'");

          foreach ($result as $key) {
            $rpass = $key[$pass];
            if($key['VK_ID'] != $user_id) {
              $msg = "Данный аккаунт Вам не пренадлежит.";
              break;
            }
            if($rpass != $password) {
                    $msg = "Пароль введён не верно.";
                  break;
            } else {
                $mysqli->query("UPDATE `{$database}` SET `VK_ID` = '' WHERE `$nick` = '{$login}'");
                $msg = "Учётная запись {$login} успешно отвязана. Но всё же, мы не советуем этого делать.";
            }
          }       	
      	break;

      case 'инфо':
         $result = $mysqli->query("SELECT * FROM `{$database}` WHERE `VK_ID` = '{$user_id}'");

         if($result->num_rows == 0) {
          $msg = "Вам не пренадлежит ни одного аккаунта.";
          break;
         }

         $msg = "Список Ваших аккаунтов: ";

         foreach($result as $key) {
            $msg .= "
            {$key[$nick]}";
         }

        break;

      case 'восстановить':
          $data = explode(" ", $message);

          if(!isset($data[1])) {
              $msg = "Использование: восстановить <ник>";
              break;
          }

          $nickname = strtolower($data[1]);

          $result = $mysqli->query("SELECT * FROM `{$database}` WHERE `VK_ID` = '{$user_id}' AND `{$nick}` = '{$nickname}'");
          if($result->num_rows == 0) {
            $msg = "Данный аккаунт Вам не пренадлежит.";
            break;
         }

         $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
         $charactersLength = strlen($characters);
         $randomString = "";
         for ($i = 0; $i < 12; $i++) {
           $randomString .= $characters[rand(0, $charactersLength - 1)];
         }

         $pshash = hash($hash, $randomString);

         $mysqli->query("UPDATE `{$database}` SET `{$pass}` = '{$pshash}' WHERE `{$nick}` = '{$nickname}'");
         $msg = "Ваш новый пароль для аккаунта {$data[1]} - {$randomString}.

         Сохраните его и удалите это сообщение в целях безопасности.";
         break;
       
       default: break;
     } 

          $request_params = array( 
              'message' => $msg, 
              'user_id' => $user_id, 
              'access_token' => $token, 
              'v' => '5.0' 
              ); 

              $get_params = http_build_query($request_params); 

             file_get_contents('https://api.vk.com/method/messages.send?'. $get_params); 
             echo('ok');

break; 

} 
?> 