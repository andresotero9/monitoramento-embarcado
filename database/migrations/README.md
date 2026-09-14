# Migrations

As migrations Flyway deste projeto ficam em:

```
backend/src/main/resources/db/migration/
```

Esse é o local padrão reconhecido automaticamente pelo Spring Boot + Flyway
(`classpath:db/migration`), sem necessidade de configuração adicional em
`application.yml`.

Este diretório (`database/migrations/`) foi mantido apenas como referência da
estrutura original do projeto — não contém arquivos executáveis. Para ver ou
editar as migrations, acesse o caminho acima.
