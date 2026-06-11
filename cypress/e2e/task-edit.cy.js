describe('Тестирование формы редактирования задачи и СППР', () => {
  beforeEach(() => {
    cy.visit('/login')
    cy.get('input[placeholder*="Логин"]').type('user3')
    cy.get('input[placeholder*="Пароль"]').type('user3')
    cy.contains('button', 'Войти').click()
    cy.url().should('not.include', '/login')
  })
  it('При успешном входе должен открываться главный дашборд', () => {
    cy.visit('/')
    cy.contains('Дашборд').should('be.visible')
  })
  it('При нажатии на пункт меню "Задачи" должен открываться список задач', () => {
    cy.visit('/')
    cy.get('nav').contains('Задачи').click()
    cy.url().should('include', '/tasks')
    cy.contains('Мои задачи').should('be.visible')
  })
  it('Должен успешно загрузить форму и отобразить текущие навыки', () => {
    cy.visit('/tasks/edit/3')
    cy.get('input[placeholder*="например: Дизайн адаптивного лендинга"]').should('be.visible')
    cy.contains('Теги / Метки задачи').should('be.visible')
  })
  it('Должен отправить форму и обновить задачу', () => {
    cy.visit('/tasks/edit/3')
    cy.get('textarea').clear().type('Обновленное ТЗ для разработчиков. Проверить интеграцию с Jira.')
    cy.contains('button', 'Сохранить').click()
  })
})
