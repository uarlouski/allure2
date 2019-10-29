'use strict';

allure.api.addTranslation('en', {
    tab: {
        scenarioGroups: {
            name: 'Scenario Groups'
        }
    }
});

allure.api.addTranslation('en', {
    tab: {
        storyGroups: {
            name: 'Story Groups'
        }
    }
});

addTab('storyGroups', 'groups.json');
addTab('scenarioGroups', 'scenarioGroups.json');

function addTab(tabName, dataPath) {
    const nameKey = 'tab.' + tabName + '.name';
    allure.api.addTab(tabName, {
        title: nameKey,
        icon: 'fa fa-list',
        route: tabName + '(/)(:testGroup)(/)(:testResult)(/)(:testResultTab)(/)',
        onEnter: (testGroup, testResult, testResultTab) => new allure.components.TreeLayout({
            testGroup,
            testResult,
            testResultTab,
            tabName: nameKey,
            baseUrl: tabName,
            url: 'data/' + dataPath
        })
    });
}
