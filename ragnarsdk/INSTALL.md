###### Composer install
```
Nginx
  在nginx配置内include weiboad_fastcgi_pararms

例如：
server {
    listen 80;
    charset utf-8;
    root /path/xxx/xxx/src/public;
    server_name xxx.com;

    location / {
        index index.php index.html index.htm;
        if (-f $request_filename) {
            break;
        }
        if (-d $request_filename) {
            break;
        }
        if ($request_filename !~ (\.css|images|index\.php.*) ) {
            rewrite ^/(.*)$ /index.php/$1 last;
            break;
        }
    }

    location ~ /index.php/ {
        fastcgi_index index.php;
        fastcgi_pass 127.0.0.1:9000;
        include fastcgi_params;
        include weiboad_fastcgi_params; # 这里
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        fastcgi_read_timeout 600;
    }

    location ~ \.php$ {
        fastcgi_index index.php;
        fastcgi_pass 127.0.0.1:9000;
        include fastcgi_params;
        include weiboad_fastcgi_params; # 这里
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        fastcgi_read_timeout 600;
    }
}
```
> 配置好后记得reload nginx

## 请在项目内使用RagnarSDK埋点后方可使用
> 具体埋点库使用方法请参考script内demo.php及README
